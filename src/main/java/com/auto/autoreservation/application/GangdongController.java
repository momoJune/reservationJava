package com.auto.autoreservation.application;

import com.auto.autoreservation.infrastructure.GangdongVO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.*;


@Controller
public class GangdongController {
    @GetMapping("/gangdongMain")
    public String gangdongMain(Model model) {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int nextMonth = now.plusMonths(1).getMonthValue();

        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("nextMonth", nextMonth);

        // 현재 월과 다음 월의 이름을 모델에 추가
        String currentMonthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String nextMonthName = now.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        model.addAttribute("currentMonthName", currentMonthName);
        model.addAttribute("nextMonthName", nextMonthName);
        return "gangdong";
    }

    @PostMapping("/runGangMacro")
    public String runGangMacro(@ModelAttribute GangdongVO vo, Model model) {
        // 폼 데이터 처리 로직 작성
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get("https://online.igangdong.or.kr/Login.do");
            // member_id 요소가 나타날 때까지 기다림
            WebElement memberIdField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("member_id")));
            memberIdField.sendKeys(vo.getUsername());
            // member_pw 요소가 나타날 때까지 기다림
            WebElement memberPwField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("member_pw")));
            memberPwField.sendKeys(vo.getPassword());
            driver.findElement(By.cssSelector(".btn_login")).click();

            handleAlertIfPresent(driver);

            // 페이지가 "https://online.igangdong.or.kr/"로 이동할 때까지 대기
            wait.until(ExpectedConditions.urlToBe("https://online.igangdong.or.kr/"));

            // 로그인 후 지정된 URL로 이동
            driver.get("https://online.igangdong.or.kr/rent/agree.do");

            //동의체크후 확인버튼 클릭
            driver.findElement(By.id("agree1")).click();
            driver.findElement(By.cssSelector(".btn_blue")).click();

            //페이지의 년.월(ex 2024.7)이 나의 요청 년.월 이랑 다를경우 다음 월 클릭
            String yearMonth = vo.getYear()+"."+vo.getMonth();
            WebElement strongElement = driver.findElement(By.cssSelector(".date strong"));
            String strongText = strongElement.getText().trim();


            WebElement initialRevWrap = driver.findElement(By.className("rev_wrap"));
            // WebDriverWait 설정


            //내가선택한 연,월의 요일의 날짜 구하기
            List<String> days = getSpecificWeekdays(vo);
            System.out.println(vo.getMonth()+"월의 요일별 날짜 계산 결과 : "+ days);
            boolean breakTp = false;
            // place1001부터 place1011까지 순차적으로 클릭
            for (int i = 1; i <= 11; i++) {
                if(breakTp)break;
                String placeId = "place100" + i;
                if(i>=10)placeId= "place10" + i;
                try {
                    if (i==5)driver.findElement(By.id("part1002")).click();
                    driver.findElement(By.id(placeId)).click();
                    System.out.println(i+"코트 클릭성공");

                    //월 선택 넘기기
                    if (!strongText.equals(yearMonth)) {
                        driver.findElement(By.cssSelector(".btn_next")).click();
                    }
                    
                    for (String day : days) {
                        try {
                            // 날짜 요소 찾기
                            // rev_wrap 요소가 변경될 때까지 대기
                            wait.until(ExpectedConditions.stalenessOf(initialRevWrap));
                            // 새로고침된 rev_wrap 요소 가져오기
                            WebElement refreshedRevWrap = driver.findElement(By.className("rev_wrap"));
                            WebElement dateElement = refreshedRevWrap.findElement(By.xpath(".//td[contains(@id, 'td') and .//span[text()='" + day + "'] and .//p[contains(@class, 'blue')]]"));

                            // 클릭 가능한 날짜인지 확인하고 클릭
                            if (dateElement != null) {
                                dateElement.click();

                                WebElement timeLabel = driver.findElement(By.xpath("//td/label[text()='" + vo.getTime() + "']"));
                                if (timeLabel != null) {
                                    // label 요소의 for 속성을 사용하여 관련된 체크박스의 id를 찾음
                                    String checkboxId = timeLabel.getAttribute("for");
                                    WebElement checkbox = driver.findElement(By.id(checkboxId));
                                    if (checkbox != null && !checkbox.isSelected()) {
                                        checkbox.click();
                                        System.out.println(i+"코트 "+day+"일 "+vo.getTime() + "시간 대관신청 완료.");
                                        driver.findElement(By.cssSelector(".btn_area .btn_blue")).click();

                                        // 경고 창이 나타날 때까지 대기
                                        WebDriverWait waitAlert = new WebDriverWait(driver, Duration.ofSeconds(2));
                                        waitAlert.until(ExpectedConditions.alertIsPresent());

                                        // 경고 창을 수락 (확인 버튼 클릭)
//                                        Alert alert = driver.switchTo().alert();
                                        handleAlertIfPresent(driver);

                                        breakTp =true;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(i+"코트의 "+day + "일은 예약가능 날짜가 아닙니다.");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Element with id " + placeId + " not found or not clickable.");
                }
            }
            if(breakTp){
                // 버튼이 클릭 가능한 상태가 될 때까지 대기
                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_navy.g-recaptcha")));
                // input 박스에 텍스트 입력
                WebElement inputBox = driver.findElement(By.name("Use_Inwon"));
                if (inputBox != null) {
                    inputBox.clear(); // 기존 값이 있다면 지우기
                    inputBox.sendKeys(vo.getNumPeople()+""); // 원하는 값 입력
                }

                button.click();
                // 경고 창이 나타날 때까지 대기
                WebDriverWait waitAlert = new WebDriverWait(driver, Duration.ofSeconds(2));
                waitAlert.until(ExpectedConditions.alertIsPresent());

                // 경고 창을 수락 (확인 버튼 클릭)


//                alert.accept();
            }


            model.addAttribute("gangVO", vo);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "테스트 실행 중 오류가 발생했습니다.");
        } finally {
            //매크로 실행이 끝난후 작업

        }
        return "gangResult";  // 결과 페이지로 이동
    }

    public static List<String> getSpecificWeekdays(GangdongVO vo) {
        List<String> dates = new ArrayList<>();

        int year = vo.getYear();
        Month month = getMonth(vo.getMonth());
        Set<DayOfWeek> daysOfWeek = new HashSet<>();
        getDay(daysOfWeek,vo.getDay());
        System.out.println("요일: " + daysOfWeek);
        // 시작 날짜와 종료 날짜
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // DateTimeFormatter 정의: 앞의 0을 제거하는 형식
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.DAY_OF_MONTH)
                .toFormatter();

        // 각 날짜를 순회하며 지정된 요일 체크
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (daysOfWeek.contains(date.getDayOfWeek())) {
                dates.add(date.format(formatter));
            }
        }

        return dates;
    }
    public static void getDay(Set<DayOfWeek> daysOfWeek,List<String> vo ) {
        // 예시: 화요일, 수요일, 금요일을 포함하는 집합

        List<String> stringDay = vo;
        for (int i = 0, len = stringDay.size(); i < len; i++) {
            String day = stringDay.get(i);
            switch (day) {  // 입력 변수의 자료형은 byte, short, char, int, enum, String만 가능하다.
                case "월":
                    daysOfWeek.add(DayOfWeek.MONDAY);
                    continue;
                case "화":
                    daysOfWeek.add(DayOfWeek.TUESDAY);
                    continue;
                case "수":
                    daysOfWeek.add(DayOfWeek.WEDNESDAY);
                    continue;
                case "목":
                    daysOfWeek.add(DayOfWeek.THURSDAY);
                    continue;
                case "금":
                    daysOfWeek.add(DayOfWeek.FRIDAY);
                    continue;
                case "토":
                    daysOfWeek.add(DayOfWeek.SATURDAY);
                    continue;
                case "일":
                    daysOfWeek.add(DayOfWeek.SUNDAY);
            }

        }
    }

    public static Month getMonth ( int voMonth){
        int month = voMonth;
        switch (month) {  // 입력 변수의 자료형은 byte, short, char, int, enum, String만 가능하다.
            case 1:
                return Month.JANUARY;
            case 2:
                return Month.FEBRUARY;
            case 3:
                return Month.MARCH;
            case 4:
                return Month.APRIL;
            case 5:
                return Month.MAY;
            case 6:
                return Month.JUNE;
            case 7:
                return Month.JULY;
            case 8:
                return Month.AUGUST;
            case 9:
                return Month.OCTOBER;
            case 10:
                return Month.SEPTEMBER;
            case 11:
                return Month.NOVEMBER;
            case 12:
                return Month.DECEMBER;
        }
        return null;
    }

    public void handleAlertIfPresent(WebDriver driver) {
        try {
            // alert이 존재하는지 확인
            Alert alert = driver.switchTo().alert();
            // alert이 존재하면 수락
            alert.accept();
            System.out.println("Alert accepted");
        } catch (NoAlertPresentException e) {
            // alert이 존재하지 않는 경우
            System.out.println("No alert present");
        }
    }


}
