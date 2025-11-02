package in.swarnavo.moneymanager.service;

import in.swarnavo.moneymanager.dto.ExpenseDTO;
import in.swarnavo.moneymanager.entity.ProfileEntity;
import in.swarnavo.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile : profiles) {
            String body = "Hi " + profile.getFullName() + ",<br><br>"
                    + "This is a friendly reminder to add your income and expense for today in MoneyManager.<br><br>"
                    + "<a href="+frontendUrl+" style='display: inline-block; padding: 12px 24px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>Go to Money Manager</a>"
                    + "<br><br>Best regards,<br>Money Manager Team";
            emailService.sendEmail(profile.getEmail(), "Daily reminder: Add your income and expenses", body);
        }
        log.info("Job completed: sendDailyIncomeExpenseReminder()");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary()");
        List<ProfileEntity> profiles = profileRepository.findAll();
        for(ProfileEntity profile : profiles) {
            List<ExpenseDTO> todaysExpenses = expenseService.getExpenseForUserOnDate(profile.getId(), LocalDate.now());
            if(!todaysExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse: collapse; width: 100%; font-family: Arial, sans-serif; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");
                table.append("<tr style='background-color: #4CAF50; color: white;'><th style='border: 1px solid #ddd; padding: 12px; text-align: left;'>S.No</th><th style='border: 1px solid #ddd; padding: 12px; text-align: left;'>Name</th><th style='border: 1px solid #ddd; padding: 12px; text-align: left;'>Amount</th><th style='border: 1px solid #ddd; padding: 12px; text-align: left;'>Category</th><th style='border: 1px solid #ddd; padding: 12px; text-align: left;'>Date</th></tr>");
                int i = 1;
                for(ExpenseDTO expense : todaysExpenses) {
                    table.append("<tr>");
                    table.append("<td style='border: 1px solid #ddd; padding: 10px;'>").append(i++).append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 10px;'>").append(expense.getName()).append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 10px; font-weight: bold;'>₹").append(expense.getAmount()).append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 10px;'>").append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A").append("</td>");
                    table.append("<td style='border: 1px solid #ddd; padding: 10px;'>").append(expense.getDate()).append("</td>");
                    table.append("</tr>");
                }
                table.append("</table>");
                String body = "Hi " + profile.getFullName()+", <br><br> Here is a summary of your expenses for today:<br/><br/>" + table + "<br/><br/>Best regards,<br/>Money Manager Team";
                emailService.sendEmail(profile.getEmail(), "Your Daily Expense Summary", body);
            }
        }
        log.info("Job completed: sendDailyExpenseSummary()");
    }
}
