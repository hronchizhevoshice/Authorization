package com.example.authorization

import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

data class User(
    val id: Long,
    val email: String,
    val companyName: String
)

data class RegisterData(
    val email: String,
    val phone: String,
    val password: String,
    val companyName: String,
    val inn: String,
    val kpp: String,
    val form: String,
    val ogrn: String,
    val okpo: String,
    val companyAddress: String,
    val firstname: String,
    val lastname: String,
    val bank: String,
    val bik: String,
    val rs: String,
    val ks: String
)

data class FullUser(
    val id: Long,
    val email: String,
    val phone: String,
    val password: String,
    val companyName: String,
    val inn: String,
    val kpp: String,
    val form: String,
    val ogrn: String,
    val okpo: String,
    val companyAddress: String,
    val firstname: String,
    val lastname: String,
    val bank: String,
    val bik: String,
    val rs: String,
    val ks: String
)

object DatabaseManager {

    private const val TAG = "DB_TEST"

    private const val DB_HOST = "aws-1-eu-west-1.pooler.supabase.com"
    private const val DB_PORT = "5432"
    private const val DB_NAME = "postgres"
    private const val DB_USER = "app_writer.kbaugpcevgbmmmhmvhzr"
    private const val DB_PASSWORD = "SuPaBaSe2301BIM"

    private val connectionUrl = "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"

    fun getConnection(): Connection? {
        return try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(connectionUrl, DB_USER, DB_PASSWORD)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка getConnection: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    // ---- Регистрация ----
    fun registerUser(d: RegisterData): Result<User> {
        return try {
            val conn = getConnection()
                ?: return Result.failure(Exception("Нет соединения с БД"))
            val sql = """
                INSERT INTO public.users 
                (email, phone, password_hash, company_name, inn, kpp,
                 form, ogrn, okpo, company_adress,
                 firstname, lastname, bank, bik, r_s, k_s)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id, email, company_name
            """.trimIndent()
            val ps = conn.prepareStatement(sql)
            ps.setString(1, d.email)
            ps.setString(2, d.phone)
            ps.setString(3, d.password)
            ps.setString(4, d.companyName)
            ps.setString(5, d.inn)
            ps.setString(6, d.kpp)
            ps.setString(7, d.form)
            ps.setString(8, d.ogrn)
            ps.setString(9, d.okpo)
            ps.setString(10, d.companyAddress)
            ps.setString(11, d.firstname)
            ps.setString(12, d.lastname)
            ps.setString(13, d.bank)
            ps.setString(14, d.bik)
            ps.setString(15, d.rs)
            ps.setString(16, d.ks)
            val rs = ps.executeQuery()
            val user = if (rs.next()) {
                User(
                    id = rs.getLong("id"),
                    email = rs.getString("email"),
                    companyName = rs.getString("company_name")
                )
            } else {
                return Result.failure(Exception("INSERT не вернул строку"))
            }
            rs.close()
            ps.close()
            conn.close()
            Log.d(TAG, "Пользователь ${d.email} зарегистрирован id=${user.id}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка registerUser: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ---- Вход по ИНН ----
    fun loginUser(inn: String, password: String): User? {
        return try {
            val conn = getConnection() ?: return null
            val sql = """
                SELECT id, email, company_name 
                FROM public.users 
                WHERE inn = ? AND password_hash = ?
                LIMIT 1
            """.trimIndent()
            val ps = conn.prepareStatement(sql)
            ps.setString(1, inn)
            ps.setString(2, password)
            val result = ps.executeQuery()
            val user = if (result.next()) {
                User(
                    id = result.getLong("id"),
                    email = result.getString("email"),
                    companyName = result.getString("company_name")
                )
            } else null
            result.close()
            ps.close()
            conn.close()
            user
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка loginUser: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    // ---- Проверка, что ИНН ещё не занят ----
    fun isInnExists(inn: String): Boolean {
        return try {
            val conn = getConnection() ?: return false
            val sql = "SELECT 1 FROM public.users WHERE inn = ? LIMIT 1"
            val ps = conn.prepareStatement(sql)
            ps.setString(1, inn)
            val result = ps.executeQuery()
            val exists = result.next()
            result.close()
            ps.close()
            conn.close()
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка isInnExists: ${e.javaClass.simpleName}: ${e.message}", e)
            false
        }
    }

    // ---- Загрузка полного профиля по id ----
    fun getUserById(id: Long): FullUser? {
        return try {
            val conn = getConnection() ?: return null
            val sql = """
                SELECT id, email, phone, password_hash, company_name, inn, kpp,
                       form, ogrn, okpo, company_adress,
                       firstname, lastname, bank, bik, r_s, k_s
                FROM public.users 
                WHERE id = ?
                LIMIT 1
            """.trimIndent()
            val ps = conn.prepareStatement(sql)
            ps.setLong(1, id)
            val result = ps.executeQuery()
            val user = if (result.next()) {
                FullUser(
                    id = result.getLong("id"),
                    email = result.getString("email") ?: "",
                    phone = result.getString("phone") ?: "",
                    password = result.getString("password_hash") ?: "",
                    companyName = result.getString("company_name") ?: "",
                    inn = result.getString("inn") ?: "",
                    kpp = result.getString("kpp") ?: "",
                    form = result.getString("form") ?: "",
                    ogrn = result.getString("ogrn") ?: "",
                    okpo = result.getString("okpo") ?: "",
                    companyAddress = result.getString("company_adress") ?: "",
                    firstname = result.getString("firstname") ?: "",
                    lastname = result.getString("lastname") ?: "",
                    bank = result.getString("bank") ?: "",
                    bik = result.getString("bik") ?: "",
                    rs = result.getString("r_s") ?: "",
                    ks = result.getString("k_s") ?: ""
                )
            } else null
            result.close()
            ps.close()
            conn.close()
            user
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка getUserById: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    // ---- Обновление профиля ----
    fun updateUser(u: FullUser): Result<Unit> {
        return try {
            val conn = getConnection()
                ?: return Result.failure(Exception("Нет соединения с БД"))
            val sql = """
                UPDATE public.users SET
                    email = ?, phone = ?, password_hash = ?,
                    firstname = ?, lastname = ?,
                    bank = ?, bik = ?, r_s = ?, k_s = ?,
                    updated_at = now()
                WHERE id = ?
            """.trimIndent()
            val ps = conn.prepareStatement(sql)
            ps.setString(1, u.email)
            ps.setString(2, u.phone)
            ps.setString(3, u.password)
            ps.setString(4, u.firstname)
            ps.setString(5, u.lastname)
            ps.setString(6, u.bank)
            ps.setString(7, u.bik)
            ps.setString(8, u.rs)
            ps.setString(9, u.ks)
            ps.setLong(10, u.id)
            ps.executeUpdate()
            ps.close()
            conn.close()
            Log.d(TAG, "Пользователь ${u.id} обновлён")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка updateUser: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }
}