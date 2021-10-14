package nio_client.start;

import lombok.RequiredArgsConstructor;
import nio_client.domain.Client;
import nio_client.domain.Room;
import nio_client.repository.RoomRepository;
import nio_client.service.BroadCastService;
import nio_client.service.NetworkService;
import nio_client.service.ResponseService;
import nio_client.ui.UI;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@Component
@RequiredArgsConstructor
public class ClientMain
{
    private final UI ui;

    @EventListener(ApplicationReadyEvent.class)
    public void startClient()
    {
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
