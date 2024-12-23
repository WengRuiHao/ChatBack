package chat.customJson;

import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CustomJson<T> {
	private Head head;
	private T data;

	
	/**
	 * 如果內部類不需要依賴外部類的實例，可以將內部類定義為靜態類 
	 * public class CustomJson<T> {
	 * 		public static class InnerClass { // 靜態內部類的內容 } 
	 * }
	 * 建立靜態內部類實例 
	 * CustomJson.InnerClass innerInstance = new CustomJson.InnerClass();
	 * 
	 * 
	 * 假設 CustomJson<T> 是你的外部類，InnerClass 是它的非靜態內部類。
	 * CustomJson<String> customJson = new CustomJson<>();
	 * CustomJson<String>.InnerClass innerInstance = customJson.new InnerClass();
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Head {
		private String type; // 訊息類型
		private String timestamp; // 訊息時間戳
		private String condition;

		@Override
		public String toString() {
			return "Head [type=" + type + ", timestamp=" + timestamp + ", condition=" + condition + "]";
		}

	}

	// 產生UUID
	public static String generateCondition() {
		long timestamp = System.currentTimeMillis();
		int randomNum = new Random().nextInt(10000);
		return timestamp + "-" + randomNum;
	}

	@Override
	public String toString() {
		return "CustomJson [head=" + head + ", data=" + data + "]";
	}

}
