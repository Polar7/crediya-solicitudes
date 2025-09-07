package co.com.pragma.creditapplication.r2dbc.queries;

public class QueriesCreditApplication {

    public static final String FIND_CREDIT_APPLICATIONS_PENDING_BY_FILTERS_PAGED = """
        SELECT ca.monto       AS amount,
               ca.plazo       AS term,
               ca.email       AS emailClient,
               lt.nombre      AS loanTypeName,
               lt.tasa_interes AS interestRate,
               st.nombre      AS statusName
        FROM solicitud ca
        JOIN estados st ON ca.id_estado = st.id_estado
        JOIN tipo_prestamo lt ON ca.id_tipo_prestamo = lt.id_tipo_prestamo
        WHERE ca.id_estado IN (2,3,4)
        AND (:emailClient IS NULL OR ca.email = :emailClient)
        AND (:loanTypeName IS NULL OR lt.nombre = :loanTypeName)
        LIMIT :size OFFSET :offset
        """;

    public static final String COUNT_FIND_CREDIT_APPLICATIONS_PENDING_BY_FILTERS_PAGED = """
        SELECT COUNT(*)
        FROM solicitud ca
        JOIN estados st ON ca.id_estado = st.id_estado
        JOIN tipo_prestamo lt ON ca.id_tipo_prestamo = lt.id_tipo_prestamo
        WHERE ca.id_estado IN (2,3,4)
        AND (:emailClient IS NULL OR ca.email = :emailClient)
        AND (:loanTypeName IS NULL OR lt.nombre = :loanTypeName)
        """;

}
