package vn.clone.fahasa_backend.repository.specification;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationsBuilder {

    public static <T> Specification<T> createSpecification(String input) {
        Specification<T> mainSpec = Specification.unrestricted();

        // Regex variables
        String regex = "(?<conjunction>\\s+(?<logicalOperator>and|or)\\s+)?(?<content>(?<notOperator>not)?(?<openParentheses>\\(*)(?<name>\\S+)\\s+(?<operator>not in|\\S+)\\s+(?<value>.*?)(?<closedParentheses>\\)*)(?=\\s+(and|or)\\s+|$))";
        Pattern r = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(input);

        // Sub-expression handler variables
        Deque<Character> stack = new ArrayDeque<>();
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
                    // Create a new specification
                    Specification<T> spec = new SearchSpecification<>(name, operator, value);
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
                // Remove outer parentheses
                sb.deleteCharAt(0)
                  .deleteCharAt(sb.length() - 1);
                System.out.println("Sub-expression: " + sb);
                System.out.println("isDisjunctionSubPredicate: " + isDisjunctionSubPredicate);

                // Run this method recursively
                Specification<T> spec = createSpecification(sb.toString());
                if (isDisjunctionSubPredicate) {
                    mainSpec = mainSpec.or(spec);
                } else {
                    mainSpec = mainSpec.and(spec);
                }

                // Reset sub-expression values
                isDisjunctionSubPredicate = false;
                isInSubExpression = false;
                sb.setLength(0);
            }
        }

        return mainSpec;
    }
}
