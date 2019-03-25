
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



/*
 * ���ǵ� �� ��������, DISK �뷮 ����,
 * ���赵�� �ִ� ������ ���� üũ. ex: 70% �̻� �ʰ�
 * ��� ������ ���Ϸ� ��������(TMS)
 * ���� ���������� ��ȣȭ �Ǿ��־����.
 * 
 * �ܰ躰 ����.
 * 1�ܰ� : ���� �������� �ش� jar �����ؼ� ����� ����
 * 2�ܰ� : ���ǵ� ���������� DB�� ������. ��ȣȭ �ʿ�
 * 3�ܰ� : ũ�� �������� �ɾ ���� �������̺��� �˻��Ͽ� ���� �����ϰ�� �˻��Ͽ� TMS ���Ϸ� �߼�
 *       - TMS �߼� ��� DB���� �ʿ�, TMS ���ø� �߰� �ʿ�
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

			// 3. �н����带 �����Ѵ�.
			session.setPassword("1234");

			// 4. ���ǰ� ���õ� ������ �����Ѵ�.
			java.util.Properties config = new java.util.Properties();
			
			// 4-1. ȣ��Ʈ ������ �˻����� �ʴ´�.
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			// 5. �����Ѵ�.
			session.connect();

			// 6. sftp ä���� ����.
			channel = session.openChannel("exec");

			// 8. ä���� SSH�� ä�� ��ü�� ĳ�����Ѵ�
			ChannelExec channelExec = (ChannelExec) channel;

			System.out.println("==> Connected to" + host);

			channelExec.setCommand("df -h"); // ��θ� ���� �־���ҵ� ex: df -h /apps
			channelExec.connect();
			
			
			/*��� �޾ƿ���*/
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
