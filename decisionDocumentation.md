This document is to record the rationale behind the decisions made, from a security point of view.

## User Registration
- Only an admin can register a user. Non-admins cannot register for an account on their own, to prevent people from creating bogus accounts using others' NRICs.

## User Login
- The user will log in with his/her NRIC, password, as well as connect to the tag, on one page and the error message, upon failure of any of the 3 will not reveal exactly which field or connection was wrong/unsuccessful.

## Bluetooth MFA Connection
- The browser talks to the tag first to ensure that the browser is legit so the tag won’t send the signature to the wrong agent. This will prevent a malicious browser from trying to obtain signatures from the tag.
