package jewelrock.irev.com.jewelrock.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class InitSettings extends RealmObject {

	@PrimaryKey
	private int id;

	@SerializedName("timer_AdMob")
	private String timerAdMob;

	@SerializedName("reklama_admob")
	private String reklamaAdmob;


	public void setTimerAdMob(String timerAdMob){
		this.timerAdMob = timerAdMob;
	}

	public String getTimerAdMob(){
		return timerAdMob;
	}


	public void setReklamaAdmob(String reklamaAdmob){
		this.reklamaAdmob = reklamaAdmob;
	}

	public String getReklamaAdmob(){
		return reklamaAdmob;
	}



	public int getId() {
		return id;
	}

	public InitSettings setId(int id) {
		this.id = id;
		return this;
	}


}