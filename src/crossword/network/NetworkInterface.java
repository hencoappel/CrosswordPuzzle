package crossword.network;

import java.util.List;

public interface NetworkInterface {

	public void sendMessage();

	public void sendCellUpdate();

	public void sendCrossword();

	public List<Server> getServers();

}
