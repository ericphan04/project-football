package com.swp.myleague.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;


public class CommonFunc {

    private static final String LOCATION_DIRECTORY = "src/main/resources/static/images/Storage-Files";

    public static UUID convertStringToUUID(String str) {
        return UUID.fromString(str);
    }

    public static String uploadFile(MultipartFile file) {
        File newFile = new File(LOCATION_DIRECTORY + File.separator + file.getOriginalFilename());
        try {

            Files.copy(file.getInputStream(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "/images/Storage-Files/" + file.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // public static User getCurrentUser(Principal principal) {
    //     String username = principal.getName();
    //     User user = userService.findByUsername(username);
    // }

}
