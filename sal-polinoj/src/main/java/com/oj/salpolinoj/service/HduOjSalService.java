package com.oj.salpolinoj.service;

import com.github.houbb.html2md.util.Html2MdHelper;
import com.oj.commonpolinoj.OJErrorCode;
import com.oj.commonpolinoj.OJException;
import com.oj.commonpolinoj.PageResult;
import com.oj.commonpolinoj.dto.*;
import com.oj.commonpolinoj.enums.OjName;
import com.oj.commonpolinoj.enums.SubmitStatus;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HduOjSalService {


    public String loginGetCookie() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username", "1144560553")
                .addFormDataPart("userpass", "e2.71828")
                .addFormDataPart("login", "Sign+In")
                .build();
        Request request = new Request.Builder()
                .url("http://acm.hdu.edu.cn/userloginex.php?action=login")
                .method("POST", body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String cookie = response.header("Set-Cookie");
            log.info("cookie: [{}]", cookie);
            return cookie;
        } catch (IOException e) {
            log.error("login error:", e);
            return "null";
        }
    }


    public PageResult<RemoteProblemDTO> pageProblem(ProblemRemotePageDTO problemPageDTO) {
        int pageIndex = Math.toIntExact(problemPageDTO.getPageIndex());
        int pageSize = Math.toIntExact(problemPageDTO.getPageSize());

        int itemBegin = (pageIndex - 1) * pageSize;
        int itemEnd = itemBegin + pageSize - 1;
        int hduPageBegin = itemBegin / 100 + 1;
        int hduPageEnd = itemEnd / 100 + 1;

        List<RemoteProblemDTO> list = new ArrayList<>();
        for (int i = hduPageBegin; i <= hduPageEnd; i++) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://acm.hdu.edu.cn/listproblem.php?vol=" + i)
                    .method("GET", null)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String html = response.body().string();
                // document.body.children[2].children[0]
                // .children[5].children[0].children[0].children[0]
                Document document = Jsoup.parse(html);
                String[] split = document.body()
                        .childNode(3)
                        .childNode(1)
                        .childNode(10)
                        .childNode(1)
                        .childNode(1)
                        .childNode(0)
                        .childNode(1)
                        .childNode(0)
                        .outerHtml().split(";");
                List<RemoteProblemDTO> problemDTOS = Arrays.stream(split)
                        .map(o -> o.substring(2, o.length() - 1))
                        .map(o -> o.split(","))
                        .map(o -> {
                            RemoteProblemDTO problemDTO = new RemoteProblemDTO();
                            problemDTO.setSourceId(o[1]);
                            problemDTO.setTitle(o[3]);
//                            problemDTO.setSampleDTOList(new ArrayList<>());
                            problemDTO.setSource(OjName.HDU_NAME);
                            try {
                                problemDTO.setAcCount(Long.valueOf(o[4]));
                                problemDTO.setAllCount(Long.valueOf(o[5]));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            int itemIndex = Integer.valueOf(o[1]) - 1000;
                            if (itemBegin <= itemIndex && itemIndex <= itemEnd) {
                                problemDTO.setUrl("http://acm.hdu.edu.cn/showproblem.php?pid=" + (itemIndex + 1000));
                                list.add(problemDTO);
                            }
                            return problemDTO;
                        }).collect(Collectors.toList());
            } catch (IOException e) {
                log.error("pageProblem error ", e);
                throw OJException.buildOJException(OJErrorCode.UNKNOWN_ERROR);
            }
        }
        PageResult<RemoteProblemDTO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPageSize(pageSize);
        pageResult.setPageIndex(pageIndex);
        pageResult.setTotal(5937);
        return pageResult;
    }

    public ProblemDTO getProblem(String id) {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("http://acm.hdu.edu.cn/showproblem.php?pid=" + id)
                .method("GET", null)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Document document = Jsoup.parse(html);
            document.outputSettings().prettyPrint(false);

            List<String[]> panelContent = document.getElementsByClass("panel_title")
                    .stream()
                    .map(o -> {
                        String k = Html2MdHelper.convert(o.html());
                        String v = Html2MdHelper.convert(o.nextElementSibling().html());
                        //System.out.println(k + " " + v);
                        return new String[]{k, v, o.nextElementSibling().html()};
                    })
                    .collect(Collectors.toList());

            ProblemDTO problemDTO = new ProblemDTO();
            problemDTO.setSample(new ArrayList<>());


            // title
            String title = document.body().child(1).child(0).child(3).child(0).child(0).text();
            problemDTO.setTitle(title);

            final SampleDTO[] sampleDTO = new SampleDTO[1];
            panelContent.stream()
                    .forEach(o -> {
                        String k = o[0];
                        String v = o[1];
                        String raw = o[2];

                        switch (k) {
                            case "Problem Description":
                                problemDTO.setDescription(v);
                                break;
                            case "Input":
                                problemDTO.setInput(v);
                                break;
                            case "Output":
                                problemDTO.setOutput(v);
                                break;
                            case "Sample Input":
                                if (sampleDTO[0] != null) {
                                    throw new RuntimeException("e");
                                }
                                sampleDTO[0] = new SampleDTO();
                                sampleDTO[0].setInput(raw);
                                break;
                            case "Sample Output":
                                if (sampleDTO[0] == null) {
                                    sampleDTO[0] = new SampleDTO();
                                }
                                sampleDTO[0].setOutput(raw);
                                problemDTO.getSample().add(sampleDTO[0]);
                                sampleDTO[0] = null;
                                break;
                            case "Author":
                                problemDTO.setAuthor(v);
                                break;
                            case "Source":
                                problemDTO.setSource(v);
                                break;

                        }
                    });


            problemDTO.setSource(OjName.HDU_NAME);
            problemDTO.setSourceId(id);
            return problemDTO;
        } catch (Exception e) {
            log.error("submit to hdu failed ", e);
            return null;
        }
    }


    public synchronized SubmitDTO submitCode(String code, String id) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("check", " 0")
                    .addFormDataPart("_usercode", new String(Base64.getEncoder().encode(URLEncoder.encode(code).getBytes())))
                    .addFormDataPart("problemid", id)
                    .addFormDataPart("language", " 0")
                    .build();
            Request request = new Request.Builder()
                    .url("http://acm.hdu.edu.cn/submit.php?action=submit")
                    .method("POST", body)
                    .addHeader("Cookie", loginGetCookie())
                    .build();
            Response response = client.newCall(request).execute();
            log.info("submitCode result: {}", response.body().string());
            SubmitDTO submitDTO = new SubmitDTO();
            submitDTO.setStatus(SubmitStatus.PENDING.getCode());
            submitDTO.setSourceSubmitId(getLastSubmitId());
            return submitDTO;

        } catch (Exception e) {
            log.error("submit to hdu failed ", e);
            return null;
        }
    }


    public String getLastSubmitId() {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://acm.hdu.edu.cn/status.php?first=&pid=&user=1144560553&lang=0&status=0")
                    .method("GET", null)
                    .addHeader("Cookie", loginGetCookie())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String html = response.body().string();
                Document document = Jsoup.parse(html);
                Elements table = document.getElementsByClass("table_text").get(0).child(0).children();
                // Run ID	Submit Time	Judge Status	Pro.ID	Exe.Time	Exe.Memory	Code Len.	Language	Author
                return table.subList(2, table.size())
                        .stream()
                        .findFirst()
                        .map(o -> {
                            Elements children = o.children();
                            SubmitDTO submitDTO = new SubmitDTO();

                            return children.get(0).html();
                        })
                        .get();
            }
        } catch (Exception e) {
            log.error("getLast submit from hdu failed ", e);
            return null;
        }
    }


    public SubmitDTO getSubmitById(String sourceSubmitId) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://acm.hdu.edu.cn/status.php?first=" + sourceSubmitId + "&pid=&user=&lang=0&status=0")
                    .method("GET", null)
                    .addHeader("Cookie", loginGetCookie())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String html = response.body().string();
                Document document = Jsoup.parse(html);
                Elements table = document.getElementsByClass("table_text").get(0).child(0).children();
                // Run ID	Submit Time	Judge Status	Pro.ID	Exe.Time	Exe.Memory	Code Len.	Language	Author
                return table.subList(2, table.size())
                        .stream()
                        .findFirst()
                        .map(o -> {
                            Elements children = o.children();
                            SubmitDTO submitDTO = new SubmitDTO();
//                            submitDTO.setSubmitTime(children.get(1).html());

                            String status = Html2MdHelper.convert(children.get(2).html());
                            submitDTO.setStatus(SubmitStatus.converter(status).getCode());
//                            submitDTO.setProblemId(Long.valueOf(Html2MdHelper.convert(children.get(3).text())));
                            String execTime = children.get(4).html();
                            submitDTO.setExecTime(Long.valueOf(execTime.substring(0, execTime.length() - 2)));
                            String execMemory = children.get(5).html();
                            submitDTO.setExecMemory(Long.valueOf(execMemory.substring(0, execMemory.length() - 1)));
                            //submitDTO.setUserId(children.get(8).text());
                            return submitDTO;
                        })
                        .get();
            }
        } catch (Exception e) {
            log.error("getResult from hdu failed ", e);
            return null;
        }
    }


    public List<SubmitDTO> getProblemSubmitResult(String problemId) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("http://acm.hdu.edu.cn/status.php?first=&pid=" + problemId + "&user=&lang=0&status=0")
                    .method("GET", null)
                    .addHeader("Cookie", loginGetCookie())
                    .build();
            try (Response response = client.newCall(request).execute()) {
                String html = response.body().string();
                Document document = Jsoup.parse(html);
                Elements table = document.getElementsByClass("table_text").get(0).child(0).children();
                // Run ID	Submit Time	Judge Status	Pro.ID	Exe.Time	Exe.Memory	Code Len.	Language	Author
                return table.subList(2, table.size())
                        .stream()
                        .map(o -> {
                            Elements children = o.children();
                            SubmitDTO submitDTO = new SubmitDTO();
//                            submitDTO.setSubmitTime(children.get(1).html());

                            String status = Html2MdHelper.convert(children.get(2).html());
                            submitDTO.setStatus(SubmitStatus.converter(status).getCode());
//                            submitDTO.setProblemId(Long.valueOf(Html2MdHelper.convert(children.get(3).text())));
                            String execTime = children.get(4).html();
                            submitDTO.setExecTime(Long.valueOf(execTime.substring(0, execTime.length() - 2)));
                            String execMemory = children.get(5).html();
                            submitDTO.setExecMemory(Long.valueOf(execMemory.substring(0, execMemory.length() - 1)));
                            //submitDTO.setUserId(children.get(8).text());
                            return submitDTO;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getResult from hdu failed ", e);
            return null;
        }
    }
}
