package com.auto.autoreservation.application;

import com.auto.autoreservation.infrastructure.GangdongVO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class GangdongController {
    @GetMapping("/gangdongMain")
    public String gangdongMain(Model model) {
        return "gangdong";
    }

    @PostMapping("/runGangMacro")
    public String runGangMacro(@ModelAttribute GangdongVO vo, Model model) {
        // 폼 데이터 처리 로직 작성
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://online.igangdong.or.kr/Login.do");
            driver.findElement(By.id("member_id")).sendKeys(vo.getUsername());
            driver.findElement(By.id("member_pw")).sendKeys(vo.getPassword());
            driver.findElement(By.cssSelector(".btn_login")).click();

            model.addAttribute("gangVO", vo);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "테스트 실행 중 오류가 발생했습니다.");
        } finally {
            //매크로 실행이 끝난후 작업

        }
        return "gangResult";  // 결과 페이지로 이동
    }
}
