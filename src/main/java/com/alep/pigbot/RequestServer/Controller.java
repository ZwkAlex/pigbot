package com.alep.pigbot.RequestServer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class Controller {
    @RequestMapping(value = "/image",method = RequestMethod.GET,produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] getImage(@RequestParam("id") String id){
        try{
            File image = new File("./local_data/"+id+".png");
            FileInputStream inputStream = new FileInputStream(image);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, inputStream.available());
            return bytes;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
