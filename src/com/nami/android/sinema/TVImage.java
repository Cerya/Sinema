package com.nami.android.sinema;

public enum TVImage {
	VIJF, ACHT, EEN, CARTOONNETWORK, CANVAS, DISCOVERY, TOBE, NICKELODEON, VITAYA, VT4, VTM, TMF, JIMTV, NATGEO, MTV,
	P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11;
	
	public String getProgess(TVImage i){
		String s = "";
		switch(i){
		case P1:
			s = "p1";
			break;
		case P2:
			s = "p2";
			break;
		case P3:
			s = "p3";
			break;
		case P4:
			s = "p4";
			break;
		case P5:
			s = "p5";
			break;
		case P6:
			s = "p6";
			break;
		case P7:
			s = "p7";
			break;
		case P8:
			s = "p8";
			break;
		case P9:
			s = "p9";
			break;
		case P10:
			s = "p10";
			break;
		case P11:
			s = "p11";
			break;
		default:
			s = "p1";
			break;
		}
		return s+".png";
	}
	
	@Override 
	public String toString() {
		//only capitalize the first letter
		//String s = super.toString();
		//return s.substring(0, 1) + s.substring(1).toLowerCase();
		String s = "";
		switch(this){
			case VIJF:
				s = "5TV";
				break;
			case MTV:
				s = "MTV";
				break;
			case ACHT:
				s = "ACH";
				break;
			case EEN:
				s = "BRT";
				break;
			case TMF:
				s = "TMF";
				break;
			case JIMTV:
				s = "JIM";
				break;
			case NATGEO:
				s = "NGC";
				break;
			case CARTOONNETWORK:
				s = "CAR";
				break;
			case CANVAS:
				s = "CAV";
				break;
			case DISCOVERY:
				s = "DIV";
				break;
			case TOBE:
				s = "KA2";
				break;
			case NICKELODEON:
				s = "NIC";
				break;
			case VITAYA:
				s = "VIT";
				break;
			case VT4:
				s = "VT4";
				break;
			case VTM:
				s = "VTM";
				break;
			default:
				s = "KA2";
				break;
		}
		return s+".png";
	}
	
}
