package com.example.fadeandfaith

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                phone TEXT NOT NULL,
                location TEXT DEFAULT '',
                password_hash TEXT NOT NULL DEFAULT '',
                firebase_uid TEXT,
                role TEXT NOT NULL DEFAULT 'CUSTOMER',
                created_at TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE services (
                service_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                price REAL NOT NULL,
                duration INTEGER NOT NULL,
                active INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE barbers (barber_id INTEGER PRIMARY KEY AUTOINCREMENT,
user_id INTEGER,
name TEXT NOT NULL,
role TEXT,
description TEXT,
rating REAL DEFAULT 0,
experience INTEGER DEFAULT 0,
active INTEGER DEFAULT 1
)
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE bookings (
                booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                service_id INTEGER NOT NULL,
                barber_id INTEGER NOT NULL,
                booking_date TEXT NOT NULL,
                booking_time TEXT NOT NULL,
                price REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'UPCOMING',
                created_at TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(user_id),
                FOREIGN KEY(service_id) REFERENCES services(service_id),
                FOREIGN KEY(barber_id) REFERENCES barbers(barber_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE payments (
                payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                booking_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                payment_method TEXT NOT NULL,
                payment_status TEXT NOT NULL,
                transaction_reference TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY(booking_id) REFERENCES bookings(booking_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE notifications (
                notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                type TEXT NOT NULL,
                is_read INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(user_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE barber_availability (
                availability_id INTEGER PRIMARY KEY AUTOINCREMENT,
                barber_id INTEGER NOT NULL,
                availability_date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                available INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(barber_id) REFERENCES barbers(barber_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE barber_service_prices (
                barber_price_id INTEGER PRIMARY KEY AUTOINCREMENT,
                barber_id INTEGER NOT NULL,
                service_id INTEGER NOT NULL,
                price REAL NOT NULL,
                UNIQUE(barber_id, service_id),
                FOREIGN KEY(barber_id) REFERENCES barbers(barber_id),
                FOREIGN KEY(service_id) REFERENCES services(service_id)
            )
        """.trimIndent())

        seedServices(db)
        seedBarbers(db)
        seedAvailability(db)
    }
    private fun seedAvailability(db: SQLiteDatabase) {

        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        val calendar = java.util.Calendar.getInstance()

        for (day in 0 until 30) {

            val date = dateFormat.format(calendar.time)

            for (barberId in 1L..3L) {

                val values = ContentValues().apply {
                    put("barber_id", barberId)
                    put("availability_date", date)
                    put("start_time", "09:00")
                    put("end_time", "17:00")
                    put("available", 1)
                }

                db.insert(
                    "barber_availability",
                    null,
                    values
                )
            }

            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
    }
    private fun seedServices(db: SQLiteDatabase) {

        insertService(
            db,
            "Haircut & Beard Trim",
            "Precision haircut finished with a professional beard trim and styling.",
            180.0,
            45
        )

        insertService(
            db,
            "Shave & Haircut",
            "Classic barbering combined with a clean shave and expertly finished haircut.",
            220.0,
            60
        )

        insertService(
            db,
            "Facial & Shave",
            "Refresh your skin with a relaxing facial followed by a professional shave.",
            250.0,
            60
        )

        insertService(
            db,
            "Premium Experience",
            "Complete Fade & Faith experience including haircut, beard treatment, facial and styling.",
            350.0,
            90
        )
    }

    private fun insertService(
        db: SQLiteDatabase,
        name: String,
        description: String,
        price: Double,
        duration: Int
    ) {
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("price", price)
            put("duration", duration)
            put("active", 1)
        }

        db.insert("services", null, values)
    }

    private fun seedBarbers(db: SQLiteDatabase) {

        insertBarber(
            db,
            "James",
            "Senior Barber",
            "Experienced barber specialising in modern cuts and beard styling.",
            4.9,
            7
        )

        insertBarber(
            db,
            "Michael",
            "Master Barber",
            "Specialist in fades, precision cuts and traditional barbering.",
            4.8,
            6
        )

        insertBarber(
            db,
            "David",
            "Professional Barber",
            "Professional barber focused on clean finishes and personalised styles.",
            4.7,
            5
        )
    }

    private fun insertBarber(
        db: SQLiteDatabase,
        name: String,
        role: String,
        description: String,
        rating: Double,
        experience: Int
    ) {
        val values = ContentValues().apply {
            put("name", name)
            put("role", role)
            put("description", description)
            put("rating", rating)
            put("experience", experience)
            put("active", 1)
        }

        db.insert("barbers", null, values)
    }

    fun registerUser(
        name: String,
        email: String,
        phone: String,
        passwordHash: String
    ): Long {

        val values = ContentValues().apply {
            put("name", name)
            put("email", email.lowercase(Locale.getDefault()))
            put("phone", phone)
            put("location", "")
            put("password_hash", passwordHash)
            put("role", "CUSTOMER")
            put("created_at", currentDateTime())
        }

        return writableDatabase.insert("users", null, values)
    }

    fun emailExists(email: String): Boolean {

        val cursor = readableDatabase.rawQuery(
            "SELECT user_id FROM users WHERE email = ? LIMIT 1",
            arrayOf(email.lowercase(Locale.getDefault()))
        )

        val exists = cursor.moveToFirst()
        cursor.close()

        return exists
    }


    fun findLocalUserIdByFirebaseUid(uid: String): Long {

        val cursor = readableDatabase.rawQuery(
            "SELECT user_id FROM users WHERE firebase_uid = ? LIMIT 1",
            arrayOf(uid)
        )

        val userId =
            if (cursor.moveToFirst()) cursor.getLong(0) else -1L

        cursor.close()

        return userId
    }


    fun createLocalProfileForFirebaseUser(
        uid: String,
        name: String,
        email: String,
        phone: String
    ): Long {

        val values = ContentValues().apply {
            put("name", name)
            put("email", email.lowercase(Locale.getDefault()))
            put("phone", phone)
            put("location", "")
            put("password_hash", "")
            put("firebase_uid", uid)
            put("role", "CUSTOMER")
            put("created_at", currentDateTime())
        }

        return writableDatabase.insert("users", null, values)
    }

    fun authenticateUser(
        email: String,
        passwordHash: String
    ): Long {

        val cursor = readableDatabase.rawQuery(
            """
            SELECT user_id
            FROM users
            WHERE email = ?
            AND password_hash = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(
                email.lowercase(Locale.getDefault()),
                passwordHash
            )
        )

        val userId =
            if (cursor.moveToFirst()) cursor.getLong(0) else -1L

        cursor.close()

        return userId
    }

    fun getUserRole(userId: Long): String? {

        val cursor = readableDatabase.rawQuery(
            "SELECT role FROM users WHERE user_id = ?",
            arrayOf(userId.toString())
        )

        val role =
            if (cursor.moveToFirst()) cursor.getString(0) else null

        cursor.close()

        return role
    }

    fun getFirebaseUidForUser(userId: Long): String? {

        val cursor = readableDatabase.rawQuery(
            "SELECT firebase_uid FROM users WHERE user_id = ? LIMIT 1",
            arrayOf(userId.toString())
        )

        val uid =
            if (cursor.moveToFirst()) cursor.getString(0) else null

        cursor.close()

        return uid
    }

    fun getUserName(userId: Long): String? {

        val cursor = readableDatabase.rawQuery(
            "SELECT name FROM users WHERE user_id = ? LIMIT 1",
            arrayOf(userId.toString())
        )

        val name =
            if (cursor.moveToFirst()) cursor.getString(0) else null

        cursor.close()

        return name
    }

    private fun currentDateTime(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN firebase_uid TEXT")
        }

        if (oldVersion < 3) {
            seedAvailability(db)
        }
    }

    companion object {
        private const val DATABASE_NAME = "fadeandfaith.db"
        private const val DATABASE_VERSION = 3
    }
    fun getServiceIdByName(name: String): Long? {

        val db = readableDatabase

        val cursor = db.query(
            "services",
            arrayOf("service_id"),
            "name = ? AND active = 1",
            arrayOf(name),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow("service_id"))
            }
        }

        return null
    }

    fun getBarberIdByName(name: String): Long? {

        val db = readableDatabase

        val cursor = db.query(
            "barbers",
            arrayOf("barber_id"),
            "name = ? AND active = 1",
            arrayOf(name),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow("barber_id"))
            }
        }

        return null
    }

    fun createBooking(
        userId: Long,
        serviceId: Long,
        barberId: Long?,
        bookingDate: String,
        bookingTime: String,
        price: Double
    ): Long {

        if (barberId == null) {
            return -1L
        }

        if (
            !canBookService(
                serviceId,
                barberId,
                bookingDate,
                bookingTime
            )
        ) {
            return -1L
        }

        val db = writableDatabase

        val values = ContentValues().apply {

            put(
                "user_id",
                userId
            )

            put(
                "service_id",
                serviceId
            )

            put(
                "barber_id",
                barberId
            )

            put(
                "booking_date",
                bookingDate
            )

            put(
                "booking_time",
                bookingTime
            )

            put(
                "price",
                price
            )

            put(
                "status",
                "PENDING"
            )

            put(
                "created_at",
                System.currentTimeMillis().toString()
            )
        }

        return db.insert(
            "bookings",
            null,
            values
        )
    }

    fun getBooking(bookingId: Long): android.database.Cursor? {

        val db = readableDatabase

        return db.rawQuery(
            """
        SELECT
            b.booking_id,
            b.booking_date,
            b.booking_time,
            b.price,
            b.status,
            s.name AS service_name,
            br.name AS barber_name
        FROM bookings b
        INNER JOIN services s
            ON b.service_id = s.service_id
        LEFT JOIN barbers br
            ON b.barber_id = br.barber_id
        WHERE b.booking_id = ?
        """.trimIndent(),
            arrayOf(bookingId.toString())
        )
    }

    fun getUserBookings(userId: Long): android.database.Cursor {

        val db = readableDatabase

        return db.rawQuery(
            """
        SELECT
            b.booking_id,
            b.booking_date,
            b.booking_time,
            b.price,
            b.status,
            s.name AS service_name,
            br.name AS barber_name
        FROM bookings b
        INNER JOIN services s
            ON b.service_id = s.service_id
        LEFT JOIN barbers br
            ON b.barber_id = br.barber_id
        WHERE b.user_id = ?
        ORDER BY b.booking_id DESC
        """.trimIndent(),
            arrayOf(userId.toString())
        )
    }

    fun cancelBooking(bookingId: Long): Boolean {

        val db = writableDatabase

        val values = android.content.ContentValues().apply {
            put("status", "CANCELLED")
        }

        return db.update(
            "bookings",
            values,
            "booking_id = ?",
            arrayOf(bookingId.toString())
        ) > 0
    }

    fun createPayment(
        bookingId: Long,
        amount: Double,
        paymentMethod: String,
        paymentStatus: String,
        transactionReference: String
    ): Long {

        val db = writableDatabase

        val values = android.content.ContentValues().apply {
            put("booking_id", bookingId)
            put("amount", amount)
            put("payment_method", paymentMethod)
            put("payment_status", paymentStatus)
            put("transaction_reference", transactionReference)
            put("created_at", System.currentTimeMillis())
        }

        return db.insertOrThrow(
            "payments",
            null,
            values
        )
    }

    fun updateBookingStatus(
        bookingId: Long,
        status: String
    ): Boolean {

        val db = writableDatabase

        val values = android.content.ContentValues().apply {
            put("status", status)
        }

        return db.update(
            "bookings",
            values,
            "booking_id = ?",
            arrayOf(bookingId.toString())
        ) > 0
    }
    fun getAllServices(): android.database.Cursor {

        return readableDatabase.rawQuery(
            """
        SELECT service_id, name, description, price, duration
        FROM services
        WHERE active = 1
        ORDER BY service_id ASC
        """.trimIndent(),
            null
        )
    }

    fun getService(serviceId: Long): android.database.Cursor? {

        return readableDatabase.rawQuery(
            """
        SELECT service_id, name, description, price, duration
        FROM services
        WHERE service_id = ? AND active = 1
        """.trimIndent(),
            arrayOf(serviceId.toString())
        )
    }

    fun getAllBarbers(): android.database.Cursor {

        return readableDatabase.rawQuery(
            """
        SELECT barber_id, name, role, description, rating, experience
        FROM barbers
        WHERE active = 1
        ORDER BY barber_id ASC
        """.trimIndent(),
            null
        )
    }

    fun getBarber(barberId: Long): android.database.Cursor? {

        return readableDatabase.rawQuery(
            """
        SELECT barber_id, name, role, description, rating, experience
        FROM barbers
        WHERE barber_id = ? AND active = 1
        """.trimIndent(),
            arrayOf(barberId.toString())
        )
    }
    fun getBookingPaymentAmount(bookingId: Long): Double? {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT price
        FROM bookings
        WHERE booking_id = ?
        """.trimIndent(),
            arrayOf(bookingId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getDouble(
                    it.getColumnIndexOrThrow("price")
                )
            }
        }

        return null
    }

    fun getPaymentForBooking(
        bookingId: Long
    ): android.database.Cursor? {

        return readableDatabase.rawQuery(
            """
        SELECT
            payment_id,
            booking_id,
            amount,
            payment_method,
            payment_status,
            transaction_reference,
            created_at
        FROM payments
        WHERE booking_id = ?
        ORDER BY payment_id DESC
        LIMIT 1
        """.trimIndent(),
            arrayOf(bookingId.toString())
        )
    }
    fun getStaffMembers(): android.database.Cursor {

        return readableDatabase.rawQuery(
            """
        SELECT user_id, name, email, phone, location
        FROM users
        WHERE role = 'STAFF'
        ORDER BY name ASC
        """.trimIndent(),
            null
        )
    }

    fun getStaffAvailability(
        barberId: Long,
        date: String
    ): android.database.Cursor {

        return readableDatabase.rawQuery(
            """
        SELECT
            availability_id,
            barber_id,
            availability_date,
            start_time,
            end_time,
            available
        FROM barber_availability
        WHERE barber_id = ?
        AND availability_date = ?
        ORDER BY start_time ASC
        """.trimIndent(),
            arrayOf(
                barberId.toString(),
                date
            )
        )
    }

    fun setBarberAvailability(
        barberId: Long,
        date: String,
        startTime: String,
        endTime: String,
        available: Boolean
    ): Long {

        val db = writableDatabase

        val values = android.content.ContentValues().apply {
            put("barber_id", barberId)
            put("availability_date", date)
            put("start_time", startTime)
            put("end_time", endTime)
            put("available", if (available) 1 else 0)
        }

        return db.insertOrThrow(
            "barber_availability",
            null,
            values
        )
    }

    fun getStaffBookings(): android.database.Cursor {

        return readableDatabase.rawQuery(
            """
        SELECT
            b.booking_id,
            b.booking_date,
            b.booking_time,
            b.price,
            b.status,
            u.name AS customer_name,
            u.email AS customer_email,
            s.name AS service_name,
            br.name AS barber_name
        FROM bookings b
        INNER JOIN users u
            ON b.user_id = u.user_id
        INNER JOIN services s
            ON b.service_id = s.service_id
        LEFT JOIN barbers br
            ON b.barber_id = br.barber_id
        ORDER BY
            b.booking_date ASC,
            b.booking_time ASC
        """.trimIndent(),
            null
        )
    }

    fun updateBookingStatusByStaff(
        bookingId: Long,
        status: String
    ): Boolean {

        val db = writableDatabase

        val values = android.content.ContentValues().apply {
            put("status", status)
        }

        return db.update(
            "bookings",
            values,
            "booking_id = ?",
            arrayOf(bookingId.toString())
        ) > 0
    }

    fun getBarberIdForStaff(staffUserId: Long): Long? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT barber_id
        FROM barbers
        WHERE user_id = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(staffUserId.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(
                    it.getColumnIndexOrThrow("barber_id")
                )
            }
        }

        return null
    }

    fun updateServicePrice(
        barberId: Long,
        serviceId: Long,
        price: Double
    ): Long {

        val db = writableDatabase

        val values =
            android.content.ContentValues().apply {
                put("barber_id", barberId)
                put("service_id", serviceId)
                put("price", price)
            }

        return db.insertWithOnConflict(
            "barber_service_prices",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }
    fun createStaffAccount(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Long {

        val db = writableDatabase

        val values =
            android.content.ContentValues().apply {

                put("name", name)
                put("email", email)
                put("phone", phone)
                put("location", "")
                put(
                    "password_hash",
                    PasswordHasher.hash(password)
                )
                put("role", "STAFF")
                put(
                    "created_at",
                    System.currentTimeMillis()
                )
            }

        return db.insertOrThrow(
            "users",
            null,
            values
        )
    }
    fun linkStaffToBarber(userId: Long, barberId: Long): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("user_id", userId)
        }

        return db.update(
            "barbers",
            values,
            "barber_id = ?",
            arrayOf(barberId.toString())
        ) > 0
    }
    fun getBarberForStaff(userId: Long): android.database.Cursor? {
        val db = readableDatabase

        return db.rawQuery(
            """
        SELECT *
        FROM barbers
        WHERE user_id = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(userId.toString())
        )
    }
    fun isBarberAvailable(
        barberId: Long,
        date: String,
        time: String
    ): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT availability_id
        FROM barber_availability
        WHERE barber_id = ?
        AND availability_date = ?
        AND available = 1
        AND ? >= start_time
        AND ? < end_time
        LIMIT 1
        """.trimIndent(),
            arrayOf(
                barberId.toString(),
                date,
                time,
                time
            )
        )

        val available = cursor.moveToFirst()
        cursor.close()

        return available
    }
    fun isBookingSlotTaken(
        barberId: Long,
        date: String,
        time: String
    ): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT booking_id
        FROM bookings
        WHERE barber_id = ?
        AND booking_date = ?
        AND booking_time = ?
        AND status NOT IN ('CANCELLED', 'COMPLETED')
        LIMIT 1
        """.trimIndent(),
            arrayOf(
                barberId.toString(),
                date,
                time
            )
        )

        val taken = cursor.moveToFirst()
        cursor.close()

        return taken
    }
    fun canCreateBooking(
        barberId: Long,
        date: String,
        time: String
    ): Boolean {

        if (!isBarberAvailable(barberId, date, time)) {
            return false
        }

        if (isBookingSlotTaken(barberId, date, time)) {
            return false
        }

        return true
    }
    fun getBarberAvailability(
        barberId: Long,
        date: String
    ):android.database.Cursor? {

        val db = readableDatabase

        return db.rawQuery(
            """
        SELECT *
        FROM barber_availability
        WHERE barber_id = ?
        AND availability_date = ?
        AND available = 1
        ORDER BY start_time ASC
        """.trimIndent(),
            arrayOf(
                barberId.toString(),
                date
            )
        )

    }
    fun getAvailableTimeSlots(
        barberId: Long,
        date: String,
        slotDurationMinutes: Int
    ): List<String> {

        val slots = mutableListOf<String>()

        val cursor = getBarberAvailability(
            barberId,
            date
        ) ?: return slots

        try {

            if (!cursor.moveToFirst()) {
                return slots
            }

            do {

                val startTime =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("start_time")
                    )

                val endTime =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("end_time")
                    )

                var currentMinutes =
                    timeToMinutes(startTime)

                val endMinutes =
                    timeToMinutes(endTime)

                while (
                    currentMinutes + slotDurationMinutes <= endMinutes
                ) {

                    val currentTime =
                        minutesToTime(currentMinutes)

                    if (
                        !isBookingSlotTaken(
                            barberId,
                            date,
                            currentTime
                        )
                    ) {
                        slots.add(currentTime)
                    }

                    currentMinutes += slotDurationMinutes
                }

            } while (cursor.moveToNext())

        } finally {

            cursor.close()
        }

        return slots.distinct()
    }


    private fun timeToMinutes(time: String): Int {

        val parts = time.split(":")

        if (parts.size != 2) {
            return 0
        }

        val hours =
            parts[0].toIntOrNull() ?: 0

        val minutes =
            parts[1].toIntOrNull() ?: 0

        return (hours * 60) + minutes
    }


    private fun minutesToTime(minutes: Int): String {

        val hours = minutes / 60
        val mins = minutes % 60

        return String.format(
            "%02d:%02d",
            hours,
            mins
        )
    }


    fun getServiceDuration(serviceId: Long): Int? {

        val db = readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT duration
        FROM services
        WHERE service_id = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(serviceId.toString())
        )

        return try {

            if (cursor.moveToFirst()) {

                cursor.getInt(
                    cursor.getColumnIndexOrThrow("duration")
                )

            } else {
                null
            }

        } finally {

            cursor.close()
        }
    }


    fun canBookService(
        serviceId: Long,
        barberId: Long,
        date: String,
        time: String
    ): Boolean {

        val duration =
            getServiceDuration(serviceId)
                ?: return false

        val availabilityCursor =
            getBarberAvailability(
                barberId,
                date
            ) ?: return false

        try {

            if (!availabilityCursor.moveToFirst()) {
                return false
            }

            do {

                val startTime =
                    availabilityCursor.getString(
                        availabilityCursor.getColumnIndexOrThrow(
                            "start_time"
                        )
                    )

                val endTime =
                    availabilityCursor.getString(
                        availabilityCursor.getColumnIndexOrThrow(
                            "end_time"
                        )
                    )

                val selectedStart =
                    timeToMinutes(time)

                val availabilityStart =
                    timeToMinutes(startTime)

                val availabilityEnd =
                    timeToMinutes(endTime)

                val selectedEnd =
                    selectedStart + duration

                if (
                    selectedStart >= availabilityStart &&
                    selectedEnd <= availabilityEnd
                ) {

                    if (
                        !isBookingSlotTaken(
                            barberId,
                            date,
                            time
                        )
                    ) {
                        return true
                    }
                }

            } while (availabilityCursor.moveToNext())

        } finally {

            availabilityCursor.close()
        }

        return false
    }
}