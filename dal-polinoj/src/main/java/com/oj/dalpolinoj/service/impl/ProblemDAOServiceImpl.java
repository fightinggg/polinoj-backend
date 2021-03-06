package com.oj.dalpolinoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.commonpolinoj.PageResult;
import com.oj.commonpolinoj.dto.*;
import com.oj.dalpolinoj.converter.ProblemConverter;
import com.oj.dalpolinoj.domin.Problem;
import com.oj.dalpolinoj.mapper.ProblemMapper;
import com.oj.dalpolinoj.service.ProblemDAOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemDAOServiceImpl implements ProblemDAOService {

    @Autowired
    ProblemMapper problemMapper;

    @Override
    public ProblemDTO createProblem(ProblemCreateDTO problemCreateDTO) {
        Problem problem = ProblemConverter.toProblemDO(problemCreateDTO);
        int insert = problemMapper.insert(problem);
        return ProblemConverter.toProblemDTO(problem);
    }

    @Override
    public ProblemDTO getProblem(ProblemGetDTO problemGetDTO) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(problemGetDTO.getProblemId() != null,
                "id", problemGetDTO.getProblemId());

        Problem problem = problemMapper.selectOne(queryWrapper);

        return ProblemConverter.toProblemDTO(problem);
    }

    @Override
    public PageResult<ProblemDTO> pageProblem(ProblemPageDTO problemPageDTO) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(problemPageDTO.getSource() != null,
                "source", problemPageDTO.getSource());
        Page<Problem> tPage = new Page<>(problemPageDTO.getPageIndex(), problemPageDTO.getPageSize());
        Page<Problem> problemPage = problemMapper.selectPage(tPage, queryWrapper);
        List<ProblemDTO> problemDTOS = ProblemConverter.toProblemDTOList(problemPage.getRecords());

        PageResult<ProblemDTO> problemDTOPageResult = new PageResult<>();
        problemDTOPageResult.setList(problemDTOS);
        return problemDTOPageResult;
    }


    @Override
    public ProblemDTO updateProblem(ProblemUpdateDTO problemUpdateDTO) {
        return null;
    }

    @Override
    public Boolean deleteProblem(ProblemDeleteDTO problemDeleteDTO) {
        return null;
    }
}
