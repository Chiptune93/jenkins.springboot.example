package jenkins.springboot.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello! This is test springboot application!";
    }

    @GetMapping("/bye")
    public String bye() {
        return "Bye! goodbye from test springboot application!";
    }
}
