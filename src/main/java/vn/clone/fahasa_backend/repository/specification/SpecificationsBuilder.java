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
        boolean isDisjunctionSubExpression = false;
        boolean isSubNegation = false;
        boolean isInSubExpression = false;

        while (m.find()) {
            System.out.println(m.group(0));
            System.out.println(m.group("content"));

            String match = m.group(0);
            String content = m.group("content");
            String openParentheses = m.group("openParentheses");
            String closedParentheses = m.group("closedParentheses");
            String logicalOperator = m.group("logicalOperator");
            String name = m.group("name");
            String operator = m.group("operator");
            String value = m.group("value");

            boolean isDisjunction = false;
            boolean isNegation = m.group("notOperator") != null;

            if (logicalOperator != null) {
                isDisjunction = logicalOperator.equals("or");
            }
            if ((openParentheses.isEmpty() && closedParentheses.isEmpty())
                || (!openParentheses.isEmpty() && !closedParentheses.isEmpty()
                    && openParentheses.length() == closedParentheses.length())) {
                if (isInSubExpression) {
                    sb.append(" ")
                      .append(logicalOperator)
                      .append(" ");
                    if (isNegation) {
                        sb.append("not(");
                    }
                    sb.append(name)
                      .append(" ")
                      .append(operator)
                      .append(" ")
                      .append(value);
                    if (isNegation) {
                        sb.append(")");
                    }
                } else {
                    // Create a new specification
                    Specification<T> spec = new SearchSpecification<>(name, operator, value);
                    if (isNegation) {
                        spec = Specification.not(spec);
                    }
                    if (isDisjunction) {
                        mainSpec = mainSpec.or(spec);
                    } else {
                        mainSpec = mainSpec.and(spec);
                    }
                }
                continue;
            }
            if (!openParentheses.isEmpty()) {
                if (stack.isEmpty()) {
                    isDisjunctionSubExpression = isDisjunction;
                    isSubNegation = isNegation;
                    isInSubExpression = true;
                    sb.append(content);
                } else {
                    sb.append(match);
                }
                for (char openParenthesis : openParentheses.toCharArray()) {
                    stack.push(openParenthesis);
                }
                continue;
            }
            sb.append(match);
            for (char ignored : closedParentheses.toCharArray()) {
                stack.pop();
            }
            if (stack.isEmpty()) {
                // Remove outer parentheses
                sb.deleteCharAt(sb.length() - 1);
                if (isSubNegation) {
                    sb.delete(0, 4);
                } else {
                    sb.deleteCharAt(0);
                }
                System.out.println("Sub-expression: " + sb);
                System.out.println("isDisjunctionSubExpression: " + isDisjunctionSubExpression);
                System.out.println("isSubNegation: " + isSubNegation);

                // Run this method recursively
                Specification<T> spec = createSpecification(sb.toString());
                if (isSubNegation) {
                    spec = Specification.not(spec);
                }
                if (isDisjunctionSubExpression) {
                    mainSpec = mainSpec.or(spec);
                } else {
                    mainSpec = mainSpec.and(spec);
                }

                // Reset sub-expression values
                isDisjunctionSubExpression = false;
                isSubNegation = false;
                isInSubExpression = false;
                sb.setLength(0);
            }
        }

        return mainSpec;
    }
}
