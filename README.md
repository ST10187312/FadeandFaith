Fade and Faith
YouTube link: https://youtube.com/shorts/Cjbr3rF5i5s?feature=share



 The problem

Barbershops that rely on walk-ins have long waits, unclear barber availability, double bookings and no easy way for new customers to see what a shop actually does. Fade & Faith puts booking, scheduling and the shop's work in one place.

 Features

**Customers**
- Register, log in and reset a forgotten password (Firebase Authentication)
- Browse services with live prices and meet the barbers (photo and description)
- Hairstyle Gallery: see real cuts the barbers have done, with the barber, location, time taken and price, before booking
- Book a service with a barber on a date and time that barber has marked available
- Protection against double booking: a taken slot disappears and is re-checked at confirmation
- My Bookings (upcoming, completed, cancelled), cancel a booking, view an e-receipt with a scannable **QR code**
- In-app notifications when a booking changes
- Choose how to pay at the shop (cash or card); the choice is saved with each booking

**Staff (barbers)**
- Separate staff login
- Dashboard with active appointment count and live notification when a customer books
- Set the dates and times they are available
- Edit their own profile photo and description (shown to customers)
- Upload finished hairstyles to the public gallery
- Request service price changes (these wait for the boss to approve)

**Boss**
- Everything staff can do, plus:
- Change service prices directly; the change shows on Home, Services and Service details straight away
- Approve or deny staff price requests
- Manage Staff: send any staff member a password-reset email

 Technology

| Area | Choice |
|---|---|
| Language | Kotlin (Android, minSdk 24, targetSdk 36) |
| Authentication | Firebase Authentication |
| Database (NoSQL) | Cloud Firestore |
| Photos | Compressed and stored with their Firestore record (`ImageStore.kt`) |
| QR codes | ZXing core, generated offline on the device |
| UI | XML layouts, Material Components, black-and-gold theme with serif headings |

 Firestore collections

`users` · `staff` · `barbers` · `services` · `availability` · `bookings` · `notifications` · `priceRequests` · `hairstyles`

 Project structure

- `app/src/main/java/com/example/fadeandfaith` : one Activity per screen, plus `SessionManager` (login state and role) and `ImageStore` (photo helper)
- `app/src/main/res/layout` : screen layouts
- `app/src/main/res/drawable` : buttons, cards, icons and images

 Run it yourself

1. Clone the repository and open it in Android Studio.
2. Add your own Firebase project: enable Email/Password sign-in and Cloud Firestore, then place its `google-services.json` in `app/`.
3. Create a `users` document for a boss (field `role` = `boss`) to use the staff login.
4. Run on an emulator or device.

Or just install the latest APK from the Releases page (allow "install unknown apps").

 Known limitations

- Language selection shows "coming soon"; the app is English-only in v1.0.
- Payment happens at the shop; there is no online card processing.
- Alerts appear while the app is open (real-time listeners), not as background push messages.

 Future improvements

Online payments, background push notifications, multi-language support, ratings and reviews, and moving photos to Firebase Storage.



 **Fade & Faith**: Your Fade. Your Barber. Your Time.

Release notes: Fade & Faith v1.0 (14 October 2026)

The prototype used a local on-device database with basic screens. v1.0 is a full rebuild on a cloud backend with new features.

Innovative features added in this version

- **Hairstyle Gallery.** Barbers upload photos of finished cuts with location, time taken and price. Customers browse them before booking.
- **QR-code e-receipt.** Every booking has a receipt with a QR code generated on the device.
- **Boss approval workflow.** Staff request price changes; the boss approves or denies them, or changes prices directly. Prices update live across the app.
- **Staff profiles with photos.** Barbers add their own photo and description, which customers see.
- **Boss staff management.** The boss can send staff password-reset emails from inside the app.
- **Real-time notifications.** Staff are alerted the moment a customer books with them.
- **Double-booking protection.** Booked slots are removed from the list and checked again at confirmation.

Other updates since the prototype

- Moved from a local SQL database to Firebase Authentication and Cloud Firestore (NoSQL).
- Role-based access: customer, staff and boss.
- Staff set their own availability, and customers can only book those times.
- Complete redesign: black-and-gold theme, serif headings, gold-outlined cards, gold inputs and pill buttons.
- Payment method choice (cash or card at the shop) saved on each booking.
- Fixed: crash after allowing notification permission, a missing-screen crash on Staff Availability, the e-receipt crash, and Services/Barbers "Book" returning to Home.
- Booking can now start from a chosen service or barber.

 Known limitations

- English only in this version.
- No online payments.
- Alerts show while the app is open.

St10187312 Siyabonga Makhubu
Tshimologo Mabunda ST10328168
