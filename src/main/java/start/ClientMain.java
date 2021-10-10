package start;

import domain.Client;
import service.BroadCastService;
import service.NetworkService;
import service.ResponseService;
import ui.UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientMain
{
    public static void main(String[] args)
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
