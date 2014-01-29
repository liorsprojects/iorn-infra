package com.ironsource.mobile;

public enum FlowCode {

	OFFERWALL("offerwall"), STICKEEZ_HANDLE("stickeez_handle"), STICKEEZ("stickeez"), STICKEEZ_HANDEL_DIRECT("stickeez_direct_handle"), SLIDER("slider"), EVENTS("events");

	private String flowCode;

	private FlowCode(String flowCode) {
		this.flowCode = flowCode;
	}

	public String getFlowCode() {
		return flowCode;
	}

	public static FlowCode convert(String flow) {
		for (FlowCode flowCode : FlowCode.values()) {
			if(flowCode.getFlowCode().equals(flow)) {
				return flowCode;
			}
		}
		return null;
	}
	
}
