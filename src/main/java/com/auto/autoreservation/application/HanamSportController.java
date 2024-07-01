package com.auto.autoreservation.application;

import com.auto.autoreservation.infrastructure.StadiumListMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class HanamSportController {

    @GetMapping("/hanamMain")
    public String hanamMain(Model model) {

        List<String[]> stadiumOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : StadiumListMapper.getStadiumMap().entrySet()) {
            stadiumOptions.add(new String[]{entry.getKey(), entry.getValue()});
        }
        model.addAttribute("stadiumOptions", stadiumOptions);

        return "hanam";
    }


    @PostMapping("/runMacro")
    public String runMacro(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("pickedStadium") String pickedStadium,
//                           @RequestParam("month1") String month1,
//                           @RequestParam("month2") String month2,
                           Model model) {


        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://rental.hanamsport.or.kr/webrental/main.do");

            // 로그인 버튼 클릭
            WebElement loginButton = driver.findElement(By.cssSelector(".ui.cursor-p"));
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

            // 로그인 모달이 나타날 때까지 대기
            WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("memberLoginModal")));

            // 로그인 모달 내에서 아이디와 비밀번호 입력 필드 찾기
            WebElement usernameField = loginModal.findElement(By.id("id"));
            WebElement passwordField = loginModal.findElement(By.id("pw"));

            // 디버깅용 출력
//            System.out.println("로그인 모달이 나타났습니다.");
//            System.out.println("ID 필드 로드 여부: " + (usernameField != null));
//            System.out.println("비밀번호 필드 로드 여부: " + (passwordField != null));

            // 값을 입력하기 전 필드가 상호작용 가능한지 확인
            wait.until(ExpectedConditions.elementToBeClickable(usernameField));
            wait.until(ExpectedConditions.elementToBeClickable(passwordField));

            // JavaScript를 사용하여 입력
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].value='';", usernameField);
            js.executeScript("arguments[0].value='';", passwordField);
            js.executeScript("arguments[0].value='" + username + "';", usernameField);
            js.executeScript("arguments[0].value='" + password + "';", passwordField);

            // 디버깅용 출력
//            System.out.println("ID 필드에 입력된 값: " + usernameField.getAttribute("value"));
//            System.out.println("비밀번호 필드에 입력된 값: " + passwordField.getAttribute("value"));

            // 로그인 버튼 클릭
            WebElement submitButton = loginModal.findElement(By.cssSelector(".ui.fluid.large.primary.submit.button"));
//            wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
            submitButton.click();
            // 로그인 모달이 사라질 때까지 대기
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("memberLoginModal")));

//            // "대관신청" 버튼 클릭
            WebElement rentalApplicationButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), '대관신청')]")));
            rentalApplicationButton.click();

            // 장소 선택 드롭다운 클릭
            WebElement selectStadium = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".search")));
            selectStadium.click();

            // "장소선택" 드롭다운 메뉴가 나타날 때까지 대기
            WebElement dropdownMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".menu.transition.visible")));

            // 특정 드롭다운 항목 선택 (data-value 속성을 기반으로)
            WebElement stadiumItem = dropdownMenu.findElement(By.cssSelector(".item[data-value='" + pickedStadium + "']"));
            stadiumItem.click();


            model.addAttribute("message", "테스트가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "테스트 실행 중 오류가 발생했습니다.");
        } finally {
            //매크로 실행이 끝난후 작업

        }
        return "result";
    }
}
