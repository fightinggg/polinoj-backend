package com.oj.bizpolinoj.service.atom;

import com.oj.commonpolinoj.PageResult;
import com.oj.commonpolinoj.dto.*;

public interface ContextService {
    ContextDTO createContext(ContextCreateDTO createDTO);

    ContextDTO updateContext(ContextUpdateDTO updateDTO);

    ContextDTO getContext(ContextGetDTO contextGetDTO);

    PageResult<ContextDTO> pageContext(ContextPageDTO pageDTO);
}
