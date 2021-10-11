package nio_client.start;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Client;
import nio_client.service.BroadCastService;
import nio_client.service.NetworkService;
import nio_client.service.ResponseService;
import nio_client.ui.UI;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Component
public class ClientMain
{
    @PostConstruct
    public void startClient()
    {
        Client client = new Client();
        ResponseService responseService = new ResponseService(client);
        BroadCastService broadCastService = new BroadCastService(client);
        NetworkService networkService = new NetworkService(responseService, broadCastService);
        UI ui = new UI(client, networkService);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            System.out.println("입력 가능...");
            String input;
            while((input = br.readLine()) != null)
            {
                ui.processInput(input);
            }
        }
        catch(IOException e){}
    }
}
