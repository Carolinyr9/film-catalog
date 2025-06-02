# Database Credentials Setup

To resolve database connection issues, update your `application.properties` file with your actual database credentials.

1. Open `src/main/resources/application.properties`.

2. Replace these lines:
    ```
    spring.datasource.username=${DB_USERNAME}
    spring.datasource.password=${DB_PASSWORD}
    ```

   With your real credentials. Exemple:
    ```
    spring.datasource.username=root
    spring.datasource.password=password
    ```

3. **Important:**  
   Do **not** commit your real credentials to a public repository.  
   Remove or change them before sharing your code.

**Never share your real credentials in public repositories.**