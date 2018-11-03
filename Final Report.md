# Final Report

## Subsystem 1 (MFA)
The 2FA tag has 2 uses in our system.
1. Login using the 2FA tag
1. Validation of uploaded files in the creation of health records for patients

The security scheme we came up with for the communication between the server and the 2FA tag makes use of both symmetric key encryption and public key cryptography. We aim to protect the
1. Confidentiality of nonce and file bytes hashes: using a fixed symmetric encryption key stored in both the tag and the server for the user
1. Integrity and authenticity: using the digital signature of the server


All our claims for 2FA tag communication will stand if the attacker does not get hold of all 3 keys - the symmetric key, the tag's private key and the server's private key. The communication will be no longer be able to continue if the MITM drops the packets. We did not store any information in the EEPROM in the tag as a future sketch would be able to read the values from the EEPROM and for the purpose of this project, we assume that the attacker has the capability to rewrite the code.

---

## Subsystems 2 to 4 Overview

Subsystems 2 to 4 support the following functionalities with the following parameters:

1. Log In
    1. NRIC number
    1. Password
    1. Role

### Login:
The user will log in with his `NRIC`, `Password`, and `Role` created by an `Administrator`. He will have a choice to login with/without a 2FA tag. If the latter is chosen, he will be required to pair his 2FA tag (if applicable) before logging in. The web app generates a unique salt for the user and adds it to his registered password. The web app generates a hash of the resultant value using SHA256 and stores both the user's hash and salt in the database of the web app. The private and public keys of each user are generated via the ED25519.

## Security for Client & Server Communication

### Session Cookie
We implemented the use of a session cookie to authenticate the user as we do not want to store any session identifiers in the local storage as that is susceptible to XSS.

### JSON Web Token (JWT)
Once the user is authenticated, a JWT will be generated in the client side for **authorisation**. This JWT will be used along the channels between Client and Server, Server and Database. In addition, the JWT will be stored in a session storage under HTML5 Web Storage. When the browser window is closed, the user will be automatically logged out. The JWT will be removed and becomes invalid.

If an incoming request contains no token, the request is denied from accessing any resources. If the request contains a token, the server side code will check if the information inside corresponds to an authorised user. If not, the request is denied.

The JWT is:
1. Signed with HMAC algorithm to prevent data tampering, thus preserving **integrity**
1. Sent via HTTPS to ensure **confidentiality** of the data in the token

In addition, using HTTPS as our only mode of transfer across channels will prevent any potential leaks from HTML5 Web Storage during transfers. It also serves as a more efficient method to ensure traffic is encrypted instead of having to deploy encryption algorithms when transferring over unsecured HTTP routes.

**Cross-Origin Resource Sharing (CORS)** will not be a potential area for exploit. The backend server limits only connection to our frontend server by specifying the IP address which it is on, hence other malicious sites would be blocked by the CORS origin policy.

---

## Subsystem 2 (Interface for Therapists & Patients)
This subsystem provides the web interface that will be used by `Therapists`, `Patients` and `Administrators` to access the Health Record System.

### Therapists Capabilities:
1. List all of his patients
1. View the profile of any of his patients
1. List and view records permitted to be viewed by him of only his patients
1. Upload records for only his patients 
1. Create new notes with regard to any patient whom he is treating
1. Edit his notes
1. Allow/disallow the patient whom a note is intended for to view the note
1. View the notes written by other therapists for his patient

### Patients Capabilities:
1. View his own records
1. Allow/disallow any of his therapists to view any of his records (A record is allowed to be viewed by his therapist who uploaded it by default)
1. View the notes permitted to be viewed by him written by his therapists for him
1. Create notes
1. Edit his notes

### Administrators Capabilities:
1. Add/delete users except himself to/from the system
1. Assign/unassign `Therapists` to `Patients`
1. Grant permission for a `Therapist` to a `Patient` to upload the latter's `Record`
1. Display logs of all transactions in the system

### Administrator's Interface
After logging in, an `Administrator` would be able to add new users under the `Manage Users` tab. He would also be able to delete any existing users except for the default `Administrator` account.

`Administrators` would be able to assign a `Therapist` to a `Patient` under the `Link Users` tab.

`Administrators` would also be able to generate server logs by choosing the date range under the `Logs` tab.

---

## Subsystem 3 (Interface for Researchers & Anyone)
This subsystem will support the functionality of retrieving anonymous data (implemented through k-anonymity), which can be filtered by:
1. Location
1. Subtype
1. Age
1. Gender

With each retrieval, the order of the data will be randomised to make it harder to re-identify each person through piecing different parts of the data.

---

## Subsystem 4 (Secure Transfer)

### Overview
`External Partners` will have a page for them to upload their database file as a .csv file.

---

## Subsystem 5 (Data Collection from Sensors)
We will be validating the files containing health record data uploaded using the 2FA tag.

---

## Security claims:
1. A **replay attack** on a tag is not possible.
    1. We have implemented the use of a nonce that is always incrementing so as to prevent replay attacks. We have also set a daily limit on the number of nonces for each user - 30.

1. **Sniffing the communication** to get the hash of the nonce and the file data hash is not possible.
    1. These two hash values (if sent over to the tag) are encrypted with the symmetric key. The IV is also always randomised so even if the server sends back the same hash of file data, the MITM is unable to see that it is the same hash.

1. The attacker, without the server's and tag's private key, **cannot be able to disguise as a legitimate server / tag**
    1. The digital signature is verified first.

1. The attacker will not be able to modify the file tagged to the patient meaningfully.
    1. We will be able to identify if the file has been changed as we schedule a check for the validation of the file digital signatures.

1. Malicious parties are unable to intercept and give incorrect public keys during transmission.
    1. Since the 2FA tags are issued by the `administrators`, the `administrators` could obtain the user's public key from the tag and store the web app's public key in the tag manually without the need for transmission of the keys.  

1. **Cross-Site Scripting (XSS)** and **SQL Injection** will not be possible for our login page.
    1. User inputs will be **escaped**. In addition, we are **validating** user inputs by implementing regex checks to ensure NRIC conforms to the standard format (eg. S1234567A). Lastly, we sanitise our `Role` input to that of a dropdown menu as there is only a few roles possible to log in as.

1. An attacker will not be able to retrieve what is in the cookie via **XSS**.
    1. The cookie is a HttpOnly cookie so no javascript code can access it.

1. An attacker will be unable to do **Cross-Site Request Forgery (CSRF)** making use of the cookie and riding on the user session.
    1. This is because our cookie has the SameSite=Strict attribute. Thus, the cookie will not be sent along with requests initiated by third party websites. Moreover, the server also checks for a sessionId value in the HTPP request header to be equal to the value stored in the JWT stored within the cookie. Thus, a legitimate request would have to comprise of both the right matching value in the HTTP request header and the cookie.

1. The user can only login and make meaningful use of our system when **HTTPS** is enabled.
    1. This is because our cookie has the Secure attribute enabled, which means that the browser will only send the cookie over a HTTPS connection. Thus, when there is no HTTPS, the user will not be authenticated by the server as the cookie is not sent over.

1. The user will not be able to retrieve the JWT value in the cookie.
   1. What we store in our cookie is encrypted so the user will be unable to see what exactly is the value being stored in the cookie.

1. Data transfer between servers are secure and not susceptible to Man-in-the-Middle attacks.
    1. The transfer stream will be restricted to the use of HTTPS so that traffic towards our database is encrypted and not susceptible to sniffing from an external party, thus preserving **confidentiality**. In addition, the data will be digitally signed using the HMAC algorithm embedded within HTTPS during upload. The digital signature can then be checked at the receiving end of the upload channel to detect whether the message has been deliberately modified, thus preserving **integrity**.

1. No invalid file types can be uploaded to the database.
    1. We are allowing only `jpg`, `png`, `txt`, `csv`, `mp4` files to be uploaded to the database.

1. No other users can create a `Patient`'s record except for the `Administrator`.
    1. The functionalities of an `Administrator` ensures that only a `Therapist` who is granted permission to access a `Patient`'s records can create, view and edit his records. This ensures **non-repudiation** such that no other users can create a `Patient`'s records if not granted permission.
