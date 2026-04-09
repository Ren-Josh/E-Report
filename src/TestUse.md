DAO Integration Test Documentation

Purpose:
This integration test verifies the functionality of the following DAO classes working together with the database:

AddUserDAO – Adds new users and credentials.
GetUserDAO – Retrieves user and credential data.
AddComplaintDAO – Adds complaints, complaint history, and complaint actions.
GetComplaintDAO – Retrieves complaint details, history, and actions.

It ensures that all DAO operations correctly persist and retrieve data, maintaining referential integrity.

1. Test AddUserDAO and GetUserDAO (DAOIntegrationTest.java)

   Test Method: testAddUserAndCredential(Connection con)

   Purpose:
   Validates the creation of a new user and their credentials, and confirms that retrieval via GetUserDAO works as expected.

   Process:
   Create a UserInfo object and populate required fields.
   Call AddUserDAO.addUser() to insert the user into the database.
   Verify that the returned user ID is valid (>0).
   Create a Credential object and call AddUserDAO.addCredential() to associate login info.
   Use GetUserDAO to retrieve the user and credential data.
   Compare retrieved data against inserted values to ensure correctness.

   Parameters:
   Connection con – Active database connection.

   Expected Output:
   Pass messages for user insertion, credential insertion, and retrieval verification.
   Failure messages if any insertion or retrieval fails.

2. Test AddUserDAO and GetUserDAO (DBIntegrationTest.java)

   Test Method: testAddUserAndCredential(Connection con)

   Purpose:
   Validates the creation of a new user and their credentials, and confirms that retrieval via GetUserDAO works as expected.

   Process:
   Create a UserInfo object and populate required fields.
   Call AddUserDAO.addUser() to insert the user into the database.
   Verify that the returned user ID is valid (>0).
   Create a Credential object and call AddUserDAO.addCredential() to associate login info.
   Use GetUserDAO to retrieve the user and credential data.
   Compare retrieved data against inserted values to ensure correctness.

   Parameters:
   Connection con – Active database connection.

   Expected Output:
   Pass messages for user insertion, credential insertion, and retrieval verification.
   Failure messages if any insertion or retrieval fails.
