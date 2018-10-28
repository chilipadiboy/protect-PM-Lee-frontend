## Instructions (to remove at the end)
In your final demo, you will demonstrate the final system behaviour, and make claims about attacks on your subsystem (what parts you think are safe). A report should be submitted by Friday 2nd of November, detailing the elements and technical elements of the final project subsystems, along with any claims you can make about it’s security behaviour. At this time your source code will be released to the other team members. In addition, at this time you will briefly demonstrate your system (the user interface, general operation, and so on) to the other team during that week.

# Final Report

## Subsystem 1 (MFA)

### Login:
The user will log in with his `NRIC`, `Password`, and `Role` created by an `Administrator`. He will have a choice to login with/without a 2FA tag. If the latter is chosen, he will be required to pair his 2FA tag (if applicable) before logging in. The web app generates a unique salt for the user and adds it to his registered password. The web app generates a hash of the resultant value using SHA256 and stores both the user's hash and salt in the database of the web app. The private and public keys of each user are generated via the ED25519.

### Security Claims:
Since the 2FA tags are issued by the administrators, the administrators could obtain the user's public key from the tag and store the web app's public key in the tag manually without the need for transmission of the keys. This eliminates the risk of malicious parties intercepting and giving incorrect public keys if the keys were to be transmitted instead.


### Health records:
TBC

### Security Claims:
TBC

---

## Subsystems 2 to 4 Overview

Subsystems 2 to 4 will support the following functionalities with the following parameters:

1. Log In
    1. NRIC number
    1. Password
    1. Role

### Security Claims:
**Cross-Site Scripting** will not be possible for our login page as user inputs will be **escaped**. In addition, we are **validating** user inputs by implementing regex checks to ensure NRIC conforms to the standard format (eg. S1234567A). Lastly, we will be sanitising our `Role` input to that of a dropdown menu as there is only a few roles possible to log in as.

## Security for Client & Server Communication

### JSON Web Token (JWT)
Once the user is authenticated, a JWT will be generated in the client side for **authorisation**. This JWT will be used along the channels between Client and Server, Server and Database. In addition, the JWT will be stored in a session storage under [HTML5 Web Storage](https://www.tutorialspoint.com/html5/html5_web_storage.htm). When the browser window is closed, the user will be automatically logged out. The JWT will be removed and becomes invalid.

If an incoming request contains no token, the request is denied from accessing any resources. If the request contains a token, the server side code will check if the information inside corresponds to an authorised user. If not, the request is denied.

### Security Claims:
**Cross-Origin Resource Sharing (CORS)** will not be a potential area for exploit.

The JWT will be:
1. Signed with HMAC algorithm to prevent data tampering, thus preserving **integrity**
1. Sent via HTTPS to ensure **confidentiality** of the data in the token

In addition, using HTTPS as our only mode of transfer across channels will prevent any potential leaks from HTML5 Web Storage during transfers. It also serves as a more efficient method to ensure traffic is encrypted instead of having to deploy encryption algorithms when transferring over unsecured HTTP routes.

The backend server will also only limit connection to our frontend server by specifying the IP address which it is on, hence other malicious sites would be blocked by the CORS origin policy.

### Security for Server & Database Communication

### Security Claims:
We will be protecting our system by:
1. Using HTTPS to ensure confidentiality and integrity in data transfer.
1. Allowing only `jpg`, `png`, `txt`, `csv`, `mp4` files to be uploaded to the database.
1. Running an anti-virus check when a new data file is uploaded.
1. Sanitising user input. This helps to prevent SQL injections & XSS (especially if the input field is a custom type, such as JSON).

---

## Subsystem 2 (Interface for Therapists & Patients)
This subsystem provides the web interface that will be used by `Therapists`, `Patients` and `Administrators` to access the Health Record System.

### Therapists Capabilities:
1. List all patients under their charge
1. Select and read patients' records only
1. Create new records
1. Edit their own created records
1. Print out reports

### Therapist's Interface
TBC

### Security Claims:
TBC

### Patients Capabilities:
TBC

### Patient's Interface
TBC

### Security Claims:
TBC

### Administrators Capabilities:
1. Add/delete users to/from the system
1. Grant permission for a `Therapist` to a `Patient` to create the latter's `Record`
1. Display logs of all transactions in the system

### Administrator's Interface
After logging in, an `Administrator` would be able to add new users under the `Manage Users` tab. He would also be able to delete any existing users except for the default `Administrator` account.

`Administrators` would be able to assign a `Therapist` to a `Patient` under the `Link Users` tab.

`Administrators` would also be able to generate server logs by choosing the date range under the `Logs` tab.

### Security Claims:
The functionalities of an `Administrator` ensures that only a `Therapist` who is granted permission to access a `Patient`'s records can create, view and edit his records. This ensures **non-repudiation** such that no other users can create a `Patient`'s records if not granted permission.

---

## Subsystem 3 (Interface for Researchers & Anyone)
This subsystem will support the functionality of retrieving anonymous data (implemented through k-anonymity), which can be filtered by:
1. Location
1. Subtype
1. Age
1. Gender

The minimum, average and maximum values of `Age` & `Reading` will be automatically generated. Furthermore, with each retrieval, the order of the data will be randomised to make it harder to re-identify each person through piecing different parts of the data.

### Security Claims:
TBC

---

## Subsystem 4 (Secure Transfer)

### Overview
The other team will be given a special account that can only perform these actions: Upload their database to our database, and creating other users except the `Administrator` roles for purpose of testing

### Interface
TBC

#### Security Claims:
The upload stream will be restricted to the use of HTTPS so that traffic towards our database is encrypted and not susceptible to sniffing from an external party, thus preserving **confidentiality**. In addition, the data will be digitally signed using the HMAC algorithm embedded within HTTPS during upload. The digital signature can then be checked at the receiving end of the upload channel to detect whether the message has been deliberately modified, thus preserving **integrity**.

---

## Subsystem 5 (Data Collection from Sensors)
TBC

### Security Claims:
TBC
