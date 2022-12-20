package com.anyholo.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@WebServlet("/dbcon")
public class AndroidInitialValue extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");;
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		JSONObject jObject = new JSONObject();
		JSONArray jArray = new JSONArray();
		String version="1.0.0";
		DBController dbc = new DBController();
		dbc.DBSelect(jArray,DBController.TWEET_SELECT,"","",1);
		ArrayList<String> prevTweetIds = new ArrayList<String>();
		ArrayList<String> repliedTweetIds = new ArrayList<String>();
		for(int i=0;i<jArray.size();i++) {
			JSONObject j = (JSONObject) jArray.get(i);
			if(j.get("tweetType").equals("DEFAULT")) {

			}
			else if(j.get("tweetType").equals("REPLIED_TO")) {
				repliedTweetIds.add(String.valueOf(j.get("prevTweetId"))); // 자기 자신
			}
			else
				prevTweetIds.add(String.valueOf(j.get("prevTweetId")));
		}
		for(int i = 0;i<prevTweetIds.size();i++) {//이미 뽑은 결과 중 다음 트윗이 있다면 2번 질의할 필요없이 있는 자료를 사용하기 위해 삭제
			for(int j=0;j<jArray.size();j++) {
				JSONObject jObj = (JSONObject) jArray.get(j);
				if(prevTweetIds.get(i).equals(jObj.get("tweetId"))) {
					prevTweetIds.remove(i);
					i--;
					break;
				}
			}
		}
		for(int i = 0;i<jArray.size();i++) {
			JSONObject jObj=(JSONObject)jArray.get(i);
			if(jObj.get("tweetType").equals("REPLIED_TO")) {
				if(repliedTweetIds.contains(jObj.get("prevTweetId"))) {
					ArrayList<JSONObject> temp = new ArrayList<JSONObject>();
					dbc.RepliedTweetSelect(temp,jObj);
					Collections.sort(temp, new Comparator<JSONObject>() {
						private static final String KEY_NUM = "Number";             //JSON key 변수 선언 생성
						@Override
						public int compare(JSONObject a, JSONObject b) {
							String valA = "";
							String valB = "";
							valA = (String) a.get("tweetId");
							valB = (String) b.get("tweetId");
					        return valA.compareTo(valB);
						}
					});//위에서부터 순서대로 넣기 위한 정렬
					jArray.remove(i);
					for(int j=0;j<temp.size();j++)
						jArray.add(i++,temp.get(j));
					i--;//맨 마지막에 빼줘야 마지막에 ++된거 상쇄됨
				}
			}
			else if(!(jObj.get("tweetType").equals("DEFAULT"))) {
				if(prevTweetIds.contains(jObj.get("prevTweetId"))) {
					dbc.PrevTweetSelect(jObj,String.valueOf(jObj.get("prevTweetId")));
				}
			}
		}
		LinkedHashSet<JSONObject> deduplicationHashSet = new LinkedHashSet<>();
		for(int i=0;i<jArray.size();i++)
			deduplicationHashSet.add((JSONObject) jArray.get(i));
		jObject.put("Version",version);
		jObject.put("Tweet", deduplicationHashSet);
		jArray = new JSONArray();
		dbc.DBSelect(jArray, DBController.KIRINUKI_SELECT,"","",1);
		jObject.put("Kirinuki", jArray);
		jArray = new JSONArray();
		dbc.DBSelect(jArray,DBController.MEMBER_SELECT,"","",1);
		jObject.put("Member", jArray);
		out.print(jObject);
		out.flush();
	}
}