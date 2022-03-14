package au.com.pjwin.commonlib.repo.retrofit.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import retrofit2.converter.moshi.MoshiConverterFactory
import java.math.BigDecimal
import java.util.Date

object BigDecimalAdapter {
    @FromJson
    fun fromJson(string: String) = BigDecimal(string)

    @ToJson
    fun toJson(value: BigDecimal) = value.toString()
}

val MOSHI_CONVERTER_FACTORY: MoshiConverterFactory by lazy {
    val dateMoshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .add(BigDecimalAdapter)
        .build()
    MoshiConverterFactory.create(dateMoshi)
}