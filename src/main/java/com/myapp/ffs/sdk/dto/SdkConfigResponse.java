package com.myapp.ffs.sdk.dto;

import java.util.List;

public record SdkConfigResponse (
	String env,
	String version, // ISO-8601 string
	List<FlagItem> flags
){
}
