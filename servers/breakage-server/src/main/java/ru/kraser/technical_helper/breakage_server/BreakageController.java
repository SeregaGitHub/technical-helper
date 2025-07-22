package ru.kraser.technical_helper.breakage_server;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@RequiredArgsConstructor
public class BreakageController {

    @GetMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getFromBreakage() {
        return "Hello from Employee BreakageController !!!";
    }

    @GetMapping(path = ADMIN_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getFromBreakageAdmin() {
        return "Hello from Admin BreakageController !!!";
    }

    @GetMapping(path = TECHNICIAN_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getFromBreakageTech() {
        return "Hello from Tech BreakageController !!!";
    }
}
