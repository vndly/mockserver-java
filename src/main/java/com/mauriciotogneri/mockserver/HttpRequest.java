package com.mauriciotogneri.mockserver;

import com.mauriciotogneri.javautils.Encoding;
import com.mauriciotogneri.javautils.Json;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

public class HttpRequest
{
    private final String method;
    private final String route;
    private final String path;
    private final List<String> cookies;
    private final Map<String, String> headers;
    private final Buffer body;

    public HttpRequest(RecordedRequest recordedRequest)
    {
        this.method = recordedRequest.getMethod();
        this.route = recordedRequest.getPath();
        this.path = pathRoute(route);
        this.cookies = cookies(recordedRequest.getHeaders());
        this.headers = headers(recordedRequest.getHeaders());
        this.body = recordedRequest.getBody();
    }

    private String pathRoute(String route)
    {
        int paramsStart = route.indexOf("?");

        return (paramsStart == -1) ? route : route.substring(0, paramsStart);
    }

    private List<String> cookies(Headers headers)
    {
        List<String> result = new ArrayList<>();

        for (String name : headers.names())
        {
            if (name.toLowerCase().equals("cookie"))
            {
                result.add(headers.get(name).trim());
            }
        }

        return result;
    }

    private Map<String, String> headers(Headers headers)
    {
        Map<String, String> result = new HashMap<>();

        for (String name : headers.names())
        {
            if (!name.toLowerCase().equals("cookie"))
            {
                result.put(name, headers.get(name).trim());
            }
        }

        return result;
    }

    public boolean matches(String method, String pattern)
    {
        return this.method.equals(method) && path.matches(pattern);
    }

    public List<String> cookies()
    {
        return cookies;
    }

    public Map<String, String> headers()
    {
        return headers;
    }

    public boolean hasHeader(String name)
    {
        return headers.containsKey(name);
    }

    public String header(String name)
    {
        return headers.get(name);
    }

    public String body()
    {
        return body.readString(Charset.forName("UTF-8"));
    }

    public <T> T body(Class<T> clazz)
    {
        return Json.object(body(), clazz);
    }

    public List<String> path(String regex)
    {
        List<String> result = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(route);

        while (matcher.find())
        {
            result.add(matcher.group(1).trim());
        }

        return result;
    }

    public String path(String regex, int index)
    {
        List<String> parameters = path(regex);

        return parameters.get(index);
    }

    public Map<String, String> query()
    {
        Map<String, String> result = new HashMap<>();

        try
        {
            String[] parts = route.split("\\?")[1].split("&");

            for (String part : parts)
            {
                String[] param = part.split("=");
                String name = param[0].trim();
                String value = (param.length > 1) ? Encoding.urlDecode(param[1]).trim() : "";

                result.put(name, value);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return result;
    }

    public String query(String name)
    {
        Map<String, String> map = query();

        return map.get(name);
    }

    public <T> T query(Class<T> clazz)
    {
        return Json.object(query(), clazz);
    }

    public Map<String, String> form()
    {
        Map<String, String> result = new HashMap<>();

        try
        {
            String[] parts = body().split("&");

            for (String part : parts)
            {
                String[] param = part.split("=");
                String name = param[0].trim();
                String value = (param.length > 1) ? Encoding.urlDecode(param[1]).trim() : "";

                result.put(name, value);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return result;
    }

    public String form(String name)
    {
        Map<String, String> map = form();

        return map.get(name);
    }

    public <T> T form(Class<T> clazz)
    {
        return Json.object(form(), clazz);
    }
}