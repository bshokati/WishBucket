package com.example.wishbucket;

import android.graphics.drawable.Drawable;

public class Deal {

	private String title;
	private String smallImageUrl;
	private String dealUrl;
	private Options options;
	private Drawable image;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSmallImageUrl() {
		return smallImageUrl;
	}
	public void setSmallImageUrl(String imageUrl) {
		this.smallImageUrl = imageUrl;
	}
	public String getDealUrl() {
		return dealUrl;
	}
	public void setDealUrl(String dealUrl) {
		this.dealUrl = dealUrl;
	}
	public String getPrice() {
		return this.options.getPrice().getFormattedAmount();
	}
	public void setPrice(String price) {
		this.options = new Options(new Price(price));
	}
	public Drawable getImage() {
		return this.image;
	}
	public void setImage(Drawable image) {
		this.image = image;
	}
	
	public class Options {
		private Price price;
		
		public Options(Price price) {
			this.price = price;
		}
		
		public void setPrice(Price price) {
			this.price = price;
		}
		
		public Price getPrice() {
			return this.price;
		}
	}
	
	public class Price {
		private String formattedAmount;
		
		public Price(String amt) {
			this.formattedAmount = amt;
		}
		
		public void setFormattedAmount(String amt) {
			this.formattedAmount = amt;
		}
		
		public String getFormattedAmount() {
			return this.formattedAmount;
		}
	}

	@Override
	public String toString() {
		return this.getTitle() + " - " + this.getPrice();
	}
	
}

