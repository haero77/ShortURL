package com.haero77.urlshortener.domain.shorturl.entity;

import static javax.persistence.GenerationType.*;

import java.time.LocalDateTime;
import java.time.Period;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.haero77.urlshortener.domain.shorturl.type.BaseTime;
import com.haero77.urlshortener.domain.shorturl.util.TimeUtil;

@Entity
public class ShortUrl extends BaseTime {

	public static final Period DEFAULT_EXPIRATION_PERIOD = Period.ofDays(7);
	public static final Period MAX_EXPIRATION_PERIOD = Period.ofDays(30);

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "short_url_id")
	private Long id;

	private String shortenedUrl;

	@Lob
	@Column(nullable = false, updatable = false)
	private String originUrl;

	private LocalDateTime expirationDate;

	@Enumerated(value = EnumType.STRING)
	private ShortUrlStatus status;

	protected ShortUrl() {
	}

	private ShortUrl(String originUrl, LocalDateTime expirationDate, ShortUrlStatus status) {
		this(null, originUrl, expirationDate, status);
	}

	private ShortUrl(String shortenedUrl, String originUrl, LocalDateTime expirationDate, ShortUrlStatus status) {
		this.shortenedUrl = shortenedUrl;
		this.originUrl = originUrl;
		this.expirationDate = expirationDate;
		this.status = status;
	}

	public static ShortUrl createWithoutShortenedUrl(
		String originUrl,
		Period expirationPeriod,
		LocalDateTime currentDateTime
	) {
		LocalDateTime expireDate = calcExpireDateTime(currentDateTime, expirationPeriod);
		return new ShortUrl(originUrl, expireDate, ShortUrlStatus.NOT_READY);
	}

	private static LocalDateTime calcExpireDateTime(LocalDateTime currentDateTime, Period expireDays) {
		return currentDateTime.plus(expireDays);
	}

	public boolean isExpired(LocalDateTime currentDateTime) {
		if (this.status == ShortUrlStatus.EXPIRED) {
			return true;
		}

		boolean isExpired = currentDateTime.isAfter(this.expirationDate);
		if (isExpired) {
			this.status = ShortUrlStatus.EXPIRED;
		}
		return isExpired;
	}

	public void assignShortenedUrl(String shortenedUrl) {
		this.shortenedUrl = shortenedUrl;
		this.status = ShortUrlStatus.ACTIVE;
	}

	public boolean hasValidStatus() {
		return !isExpired(TimeUtil.getCurrentSeoulTime()) && this.status == ShortUrlStatus.ACTIVE;
	}

	public Long id() {
		return id;
	}

	public String shortenedUrl() {
		return shortenedUrl;
	}

	public String originUrl() {
		return originUrl;
	}

	public LocalDateTime expirationDate() {
		return expirationDate;
	}

	public ShortUrlStatus status() {
		return status;
	}
}
