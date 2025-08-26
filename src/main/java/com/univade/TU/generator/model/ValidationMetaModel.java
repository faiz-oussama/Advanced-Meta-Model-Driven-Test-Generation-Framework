    package com.univade.TU.generator.model;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class ValidationMetaModel {
        
        private String attributeName;
        private String validationType;
        private String message;
        private Object value;
        private Object min;
        private Object max;
        private String pattern;
        private String[] groups;

        public boolean isNotNull() {
            return "NotNull".equals(validationType);
        }

        public boolean isNotBlank() {
            return "NotBlank".equals(validationType);
        }

        public boolean isNotEmpty() {
            return "NotEmpty".equals(validationType);
        }

        public boolean isSize() {
            return "Size".equals(validationType);
        }

        public boolean isMin() {
            return "Min".equals(validationType);
        }

        public boolean isMax() {
            return "Max".equals(validationType);
        }

        public boolean isEmail() {
            return "Email".equals(validationType);
        }

        public boolean isPattern() {
            return "Pattern".equals(validationType);
        }

        public boolean isDecimalMin() {
            return "DecimalMin".equals(validationType);
        }

        public boolean isDecimalMax() {
            return "DecimalMax".equals(validationType);
        }

        public boolean isDigits() {
            return "Digits".equals(validationType);
        }

        public boolean isFuture() {
            return "Future".equals(validationType);
        }

        public boolean isPast() {
            return "Past".equals(validationType);
        }

        public String getAnnotationString() {
            StringBuilder sb = new StringBuilder("@").append(validationType);
            
            if (hasParameters()) {
                sb.append("(");
                boolean first = true;
                
                if (value != null) {
                    sb.append("value = ").append(formatValue(value));
                    first = false;
                }
                
                if (min != null) {
                    if (!first) sb.append(", ");
                    sb.append("min = ").append(formatValue(min));
                    first = false;
                }
                
                if (max != null) {
                    if (!first) sb.append(", ");
                    sb.append("max = ").append(formatValue(max));
                    first = false;
                }
                
                if (pattern != null) {
                    if (!first) sb.append(", ");
                    sb.append("regexp = \"").append(pattern).append("\"");
                    first = false;
                }
                
                if (message != null) {
                    if (!first) sb.append(", ");
                    sb.append("message = \"").append(message).append("\"");
                }
                
                sb.append(")");
            }
            
            return sb.toString();
        }

        private boolean hasParameters() {
            return value != null || min != null || max != null || pattern != null || message != null;
        }

        private String formatValue(Object val) {
            if (val instanceof String) {
                return "\"" + val + "\"";
            }
            return val.toString();
        }
    }
