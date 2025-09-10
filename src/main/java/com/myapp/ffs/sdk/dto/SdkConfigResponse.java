package com.myapp.ffs.sdk.dto;

import java.util.List;

import com.myapp.ffs.flag.dto.FlagItem;

public record SdkConfigResponse (
	String env,
	String version, // ISO-8601 string
	List<FlagItem> flags
){
}
