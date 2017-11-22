package com.nannan.RestAPIQuerySample;

import java.util.*;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import org.json.*;

public class RestAPIQuerier 
{
	
	private String baseURL = "http://ec2-18-220-251-180.us-east-2.compute.amazonaws.com/handyMen";
	private String usrName = "sadPerson";
	private String emailAddr = "sadPerson@uwaterloo.ca";
	private String passwd = "sadPersonPasswd";
	
	static public class SimpleResponseMsg {
		int rstStatusCode;
		String responseStr;
		
		SimpleResponseMsg(int rstStatusCode, String responseStr) {
			this.rstStatusCode = rstStatusCode;
			this.responseStr = responseStr;
		}
		
		String getResponseStr() {
			return responseStr;
		}
		
		int getRstStatusCode() {
			return rstStatusCode;
		}
	}
	
	public void addUser() {
        String url = baseURL + "/addUser";
			
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("usrName", usrName));
		nvps.add(new BasicNameValuePair("emailAddr", emailAddr));
		nvps.add(new BasicNameValuePair("phoneNumList", "1234567"));
		nvps.add(new BasicNameValuePair("uploadFileNames", "test.jpg;test1.jpg"));
		nvps.add(new BasicNameValuePair("passwd", passwd));
		
		SimpleResponseMsg msg = restAPIQuery(url, nvps);
		
		if (msg.getRstStatusCode() == HttpStatus.SC_OK) {
			parseResponseMessage(msg.getResponseStr());
		}
		else {
			System.out.println("status code:" + msg.getRstStatusCode() + ", query failed");
			parseErrorResponse(msg.getResponseStr());
		}
	}
	
	private SimpleResponseMsg restAPIQuery(String url, List <NameValuePair> nvps) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		System.out.println("url:" + url);
		
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			CloseableHttpResponse response = httpclient.execute(httpPost);
			
			try {
				HttpEntity entity = response.getEntity();
				if(entity == null) {
					throw new Exception("http entity null");
				}
				
				String responseStr = EntityUtils.toString(entity);
				System.out.println("responseStr:" + responseStr);
				
				SimpleResponseMsg msg = new SimpleResponseMsg(response.getStatusLine().getStatusCode(),
						responseStr);
				
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}		
		
		return null;
	}
	
	public void addUserServiceInfo() {
		String url = baseURL + "/addUserServiceInfo";
		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("usrName", usrName));
		nvps.add(new BasicNameValuePair("type", "PLUMBER_TYPE"));
		nvps.add(new BasicNameValuePair("area", "Toronto"));
		nvps.add(new BasicNameValuePair("description", "goodGuy"));
		nvps.add(new BasicNameValuePair("priceRange", "4-30"));
		nvps.add(new BasicNameValuePair("uploadFileNames", "service.jpg;service1.jpg"));
		nvps.add(new BasicNameValuePair("passwd", passwd));
		
		SimpleResponseMsg msg = restAPIQuery(url, nvps);
		
		if (msg.getRstStatusCode() == HttpStatus.SC_OK) {
			parseResponseMessage(msg.getResponseStr());
		}
		else {
			System.out.println("status code:" + msg.getRstStatusCode() + ", query failed");
			parseErrorResponse(msg.getResponseStr());
		}
	}
	
	//for OP_OK or OP_FAIL message 
	private void parseResponseMessage(String responseMsg) {
        JSONObject jsonObject = new JSONObject(responseMsg);
		String rst = jsonObject.getString("status");
		String message = jsonObject.getString("message");
		
		System.out.println("rst:" + rst + ", message:" + message);
	}
	
	private void parseErrorResponse(String responseMsg) {
        JSONObject jsonObject = new JSONObject(responseMsg);
		String message = jsonObject.getString("message");
		System.out.println("message:" + message);
	}
	
	public void getUserProfile() {
	    String url = baseURL + "/getUserProfile";

		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("usrName", usrName));
		nvps.add(new BasicNameValuePair("passwd", passwd));
		
		SimpleResponseMsg msg = restAPIQuery(url, nvps);
		
		if (msg.getRstStatusCode() == HttpStatus.SC_OK) {
			parseUserProfile(msg.getResponseStr());
		}
		else {
			System.out.println("status code:" + msg.getRstStatusCode() + ", query failed");
			parseErrorResponse(msg.getResponseStr());
		}
	}
	
	private void parseUserProfile(String responseMsg) {
		JSONObject jsonObject = new JSONObject(responseMsg);
		parseUserProfile(jsonObject);
	}
	
	private void parseUserProfile(JSONObject jsonObject) {
        JSONObject contactInfoObject = jsonObject.getJSONObject("contactInfo");
        String usrName = contactInfoObject.getString("usrName");
        String emailAddr = contactInfoObject.getString("emailAddr");
        System.out.println("usrName:" + usrName + ", emailAddr:" + emailAddr);
        
        JSONArray serviceInfoList = jsonObject.getJSONArray("serviceInfoList");
        for (int i = 0; i < serviceInfoList.length(); i++) {
        	JSONObject serviceObj = serviceInfoList.getJSONObject(i);
        	
        	JSONObject serviceTypeObj = serviceObj.getJSONObject("svrTypeInfo");
        	parseServiceTypeInfo(serviceTypeObj);
        	
        	String area = serviceObj.getString("area");
        	String uploadFileNames = serviceObj.getString("uploadFileNames");
        	String description = serviceObj.getString("description");
        	String priceRange = serviceObj.getString("priceRange");
        	System.out.println("service info: area:" + area
        			+ ", uploadFileNames:" + uploadFileNames + ", description:" + description
        			+ ", priceRange:" + priceRange);
        }
	}
	
	private void parseServiceTypeInfo(JSONObject jsonObject) {
		String svrType = jsonObject.getString("svrType");
		int svrTypeId = jsonObject.getInt("svrTypeId");
		String svrTypeUploadFileNames = jsonObject.getString("svrTypeUploadFileNames");
		String svrTypePrice = jsonObject.getString("svrTypePrice");
		String occasion = jsonObject.getString("occasion");
		
		System.out.println("servicetype info: svrType:" + svrType
    			+ ", svrTypeId:" + svrTypeId + ", svrTypeUploadFileNames:"
    			+ svrTypeUploadFileNames + ", svrTypePrice:" +
    			svrTypePrice + ", occasion:" + occasion);
	}
	
	public void listAllServiceUsers() {
	    String url = baseURL + "/listAllServiceUsers";

		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("usrName", usrName));
		nvps.add(new BasicNameValuePair("passwd", passwd));
		
		SimpleResponseMsg msg = restAPIQuery(url, nvps);
		if (msg.getRstStatusCode() == HttpStatus.SC_OK) {
			parseServiceUsrList(msg.getResponseStr());
		}
		else {
			System.out.println("status code:" + msg.getRstStatusCode() + ", query failed");
			parseErrorResponse(msg.getResponseStr());
		}
	}
	
	private void parseServiceUsrList(String responseMsg) {
        JSONArray serviceInfoList = new JSONArray(responseMsg);
        for (int i = 0; i < serviceInfoList.length(); i++) {
        	parseUserProfile(serviceInfoList.getJSONObject(i));
        }
	}
	
	public void uploadFile() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        String url = baseURL + "/uploadFile";
		
		try {
			HttpPost httpPost = new HttpPost(url);
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			FileBody fileBody = new FileBody(new File("D:\\51721102_9.jpg"));
			builder.addPart("file", fileBody); 
			builder.addTextBody("usrName", usrName);
			builder.addTextBody("passwd", passwd);
			
			HttpEntity multiEntity = builder.build();
			httpPost.setEntity(multiEntity);
			
			CloseableHttpResponse response = httpclient.execute(httpPost);
			
			try {
				HttpEntity entity = response.getEntity();
				if(entity == null) {
					throw new Exception("http entity null");
				}
				
				String responseStr = EntityUtils.toString(entity);
				System.out.println("responseStr:" + responseStr);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public void downloadFile() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        String url = baseURL + "/uploadFile/" + "51721102_9.jpg";
        File myFile = new File("D:\\a.jpg");
		
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
			    HttpEntity entity = response.getEntity();
			    if (entity != null) {
			        try (FileOutputStream outstream = new FileOutputStream(myFile)) {
			            entity.writeTo(outstream);
			        }
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
    public static void main( String[] args )
    {	
    	RestAPIQuerier query = new RestAPIQuerier();
    	
    	query.addUser();
    	
    	query.addUserServiceInfo();
    	
    	query.getUserProfile();
    	
    	query.listAllServiceUsers();
    	
    	query.uploadFile();
    	
    	query.downloadFile();

    	
    
        
    }
}
