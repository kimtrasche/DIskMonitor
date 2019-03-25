
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



/*
 * 정의된 각 서버에서, DISK 용량 추출,
 * 위험도가 있는 서버는 별도 체크. ex: 70% 이상 초과
 * 결과 정보를 메일로 보내볼것(TMS)
 * 서버 접속정보는 암호화 되어있어야함.
 * 
 * 단계별 구축.
 * 1단계 : 기준 서버에서 해당 jar 실행해서 결과만 보기
 * 2단계 : 정의된 서버정보를 DB로 관리함. 암호화 필요
 * 3단계 : 크론 스케쥴을 걸어서 일일 휴일테이블을 검사하여 익일 휴일일경우 검사하여 TMS 메일로 발송
 *       - TMS 발송 대상 DB관리 필요, TMS 템플릿 추가 필요
 * */


public class Starter {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub

		Session session = null;
		Channel channel = null;
		String host = "192.168.1.67";
		try {
			JSch jsch = new JSch();
			session = jsch.getSession("root", host, 22);

			// 3. 패스워드를 설정한다.
			session.setPassword("1234");

			// 4. 세션과 관련된 정보를 설정한다.
			java.util.Properties config = new java.util.Properties();
			
			// 4-1. 호스트 정보를 검사하지 않는다.
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			// 5. 접속한다.
			session.connect();

			// 6. sftp 채널을 연다.
			channel = session.openChannel("exec");

			// 8. 채널을 SSH용 채널 객체로 캐스팅한다
			ChannelExec channelExec = (ChannelExec) channel;

			System.out.println("==> Connected to" + host);

			channelExec.setCommand("df -h"); // 경로를 직접 주어야할듯 ex: df -h /apps
			channelExec.connect();
			
			
			/*결과 받아오기*/
			int BUFFER_SIZE = 1024;

			byte[] buffer = new byte[BUFFER_SIZE];

			InputStream inputStream = channelExec.getInputStream();

			while (true) {
				while (inputStream.available() > 0) {
					int i = inputStream.read(buffer, 0, BUFFER_SIZE);
					if (i < 0) {
						break;
					}
					String line = new String(buffer, 0, i);

					System.out.println(line);

				}

				if (channel.isClosed()) {
					if (inputStream.available() > 0) {
						continue;
					}
					break;
				}

				TimeUnit.MILLISECONDS.sleep(100);
			}

		} catch (JSchException e) {
			e.printStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
				System.out.println("==> Disconnect to" + host);
			}
		}

	}

}
