package vn.clone.fahasa_backend.repository.specification;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.jpa.domain.Specification;

public class UserSpecificationsBuilder {

    public static <T> Specification<T> createSpecification(String input) {
        Specification<T> mainSpec = Specification.unrestricted();

        String regex = "(?<conjunction>\\s+(?<logicalOperator>and|or)\\s+)?(?<content>(?<openParentheses>\\(*)(?<name>\\S+)\\s+(?<operator>\\S+)\\s+(?<value>.*?)(?<closedParentheses>\\)*)(?=\\s+(and|or)\\s+|$))";

        // Create a Pattern object
        Pattern r = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        // Now create matcher object.
        Matcher m = r.matcher(input);

        Stack<Character> stack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        boolean isDisjunctionSubPredicate = false;
        boolean isInSubExpression = false;
        while (m.find()) {
            System.out.println(m.group(0));
            System.out.println(m.group("content"));

            String match = m.group(0);
            String content = m.group("content");
            String openParenthesesString = m.group("openParentheses");
            String closedParenthesesString = m.group("closedParentheses");
            String logicalOperator = m.group("logicalOperator");
            String name = m.group("name");
            String operator = m.group("operator");
            String value = m.group("value");
            boolean isDisjunction = false;

            if (logicalOperator != null) {
                isDisjunction = logicalOperator.equals("or");
            }
            if ((openParenthesesString.isEmpty() && closedParenthesesString.isEmpty())
                || (!openParenthesesString.isEmpty() && !closedParenthesesString.isEmpty()
                    && openParenthesesString.length() == closedParenthesesString.length())) {
                if (isInSubExpression) {
                    sb.append(" ")
                      .append(logicalOperator)
                      .append(" ")
                      .append(name)
                      .append(" ")
                      .append(operator)
                      .append(" ")
                      .append(value);
                } else {
                    Specification<T> spec = null;
                    if (isDisjunction) {
                        mainSpec = mainSpec.or(spec);
                    } else {
                        mainSpec = mainSpec.and(spec);
                    }
                }
                continue;
            }
            if (!openParenthesesString.isEmpty()) {
                String[] openParentheses = openParenthesesString.split("");
                for (String openParenthesis : openParentheses) {
                    if (stack.isEmpty()) {
                        isDisjunctionSubPredicate = isDisjunction;
                        isInSubExpression = true;
                        sb.append(content);
                    } else {
                        sb.append(match);
                    }
                    stack.push(openParenthesis.charAt(0));
                }
                continue;
            }
            sb.append(match);
            for (int i = 0; i < closedParenthesesString.length(); i++) {
                stack.pop();
            }
            if (stack.isEmpty()) {
                sb.deleteCharAt(0)
                  .deleteCharAt(sb.length() - 1);
                System.out.println("Sub-expression: " + sb);
                System.out.println("isDisjunctionSubPredicate: " + isDisjunctionSubPredicate);
                isDisjunctionSubPredicate = false;
                isInSubExpression = false;
                sb.setLength(0);

                createSpecification(sb.toString());
            }
        }
        return mainSpec;
    }
}
