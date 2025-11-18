package com.jakdang.dto;

import com.jakdang.domain.UserTag;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateUserTagsRequest {
    private Set<UserTag> tags;
}
