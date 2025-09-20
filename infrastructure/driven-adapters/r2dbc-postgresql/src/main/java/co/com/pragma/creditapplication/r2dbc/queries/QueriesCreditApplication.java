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
        WHERE st.nombre IN (:statusNames)
        AND (:emailClient IS NULL OR ca.email = :emailClient)
        AND (:loanTypeName IS NULL OR lt.nombre = :loanTypeName)
        LIMIT :size OFFSET :offset
        """;

    public static final String COUNT_FIND_CREDIT_APPLICATIONS_PENDING_BY_FILTERS_PAGED = """
        SELECT COUNT(*)
        FROM solicitud ca
        JOIN estados st ON ca.id_estado = st.id_estado
        JOIN tipo_prestamo lt ON ca.id_tipo_prestamo = lt.id_tipo_prestamo
        WHERE st.nombre IN (:statusNames)
        AND (:emailClient IS NULL OR ca.email = :emailClient)
        AND (:loanTypeName IS NULL OR lt.nombre = :loanTypeName)
        """;

    public static final String UPDATE_STATUS_BY_APPLICATIONID_AND_STATUSDESCRIPTION = """
            UPDATE solicitud
            SET id_estado = s.id_estado
            FROM estados s
            WHERE solicitud.id_solicitud = :idSolicitud
              AND s.nombre = :statusName
            """;

    public static final String FIND_ALL_BY_EMAIL_CLIENT_AND_STATUS_NAME = """
            SELECT s.monto AS amount,
                   s.plazo AS term,
                   t.tasa_interes AS interesrate
            FROM solicitud s
            JOIN estados e ON s.id_estado = e.id_estado
            JOIN tipo_prestamo t on s.id_tipo_prestamo = t.id_tipo_prestamo
            WHERE s.email = :emailClient
              AND e.nombre = :statusName
            """;
}
