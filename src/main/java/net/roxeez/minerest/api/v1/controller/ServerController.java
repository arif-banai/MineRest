package net.roxeez.minerest.api.v1.controller;

import com.google.gson.Gson;
import net.roxeez.minerest.api.v1.Controller;
import net.roxeez.minerest.api.v1.request.BroadcastRequest;
import net.roxeez.minerest.api.v1.response.BroadcastResponse;
import net.roxeez.minerest.api.v1.response.PluginResponse;
import net.roxeez.minerest.api.v1.response.ServerResponse;
import net.roxeez.minerest.http.ContentType;
import org.bukkit.Server;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.get;
import static spark.Spark.post;

public class ServerController extends Controller
{
    private final Server server;
    private final Gson gson;

    public ServerController(Server server, Gson gson)
    {
        this.server = server;
        this.gson = gson;
    }

    @Override
    public String getRoute()
    {
        return "/server";
    }

    @Override
    public void map()
    {
        get("/", this::getServer, gson::toJson);
        get("/plugins", this::getPlugins, gson::toJson);

        post("/broadcast", ContentType.APPLICATION_JSON, this::broadcast, gson::toJson);
    }

    private Object getServer(Request request, Response response)
    {
        ServerResponse output = ServerResponse.builder()
                .name(server.getName())
                .motd(server.getMotd())
                .version(server.getVersion())
                .bukkitVersion(server.getBukkitVersion())
                .gameMode(server.getDefaultGameMode().toString())
                .build();

        return Ok(response, output);
    }

    private Object broadcast(Request request, Response response)
    {
        BroadcastRequest input = gson.fromJson(request.body(), BroadcastRequest.class);
        if (input == null)
        {
            return BadRequest(response, "Unable to parse request");
        }

        int count = server.broadcast(input.message, input.permission != null ? input.permission : Server.BROADCAST_CHANNEL_USERS);

        BroadcastResponse output = BroadcastResponse.builder()
                .count(count)
                .build();

        return Ok(response, output);
    }

    private Object getPlugins(Request request, Response response)
    {
        List<PluginResponse> plugins = Arrays.stream(server.getPluginManager().getPlugins())
                .map(x -> PluginResponse.builder()
                        .name(x.getName())
                        .version(x.getDescription().getVersion())
                        .build())
                .collect(Collectors.toList());

        return Ok(response, plugins);
    }
}