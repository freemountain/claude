package org.freemountain.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.freemountain.operator.common.ResourceHash;

import javax.inject.Inject;

public class AdditionalPropsMapper {
    @Inject
    ObjectMapper mapper;

    void addResourceHash(Object resource) {
        ResourceHash.hash(mapper, )
    }


}
