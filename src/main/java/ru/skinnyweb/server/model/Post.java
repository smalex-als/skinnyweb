package ru.skinnyweb.server.model;

import java.util.Date;

import ru.skinnyweb.server.api.Id;
import ru.skinnyweb.server.api.IdFactory;

/**
 * Created by smalex on 10/08/15.
 */
public class Post extends BaseModel {
  private String source;
  private String sourceName;
  private long feedId;
  private Date created;
  private String name;
  private String description;
  private String href;
  private String src;
  private String category;
  private Date date;
  private String IMDB;
  private String kkStar;
  private String genre;
  private String year;
  private boolean archived;
  private boolean starred;
  private boolean unread;

  public Post() {
    super(IdFactory.createId("Post", 0L));
  }

  public Post(Id key) {
    super(key);
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSource() {
    return source;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public long getFeedId() {
    return feedId;
  }

  public void setFeedId(long feedId) {
    this.feedId = feedId;
  }

  public Date getCreated() {
    return created;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getHref() {
    return href;
  }

  public void setSrc(String src) {
    this.src = src;
  }

  public String getSrc() {
    return src;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Date getDate() {
    return date;
  }

  public void setIMDB(String IMDB) {
    this.IMDB = IMDB;
  }

  public String getIMDB() {
    return IMDB;
  }

  public void setKkStar(String kkStar) {
    this.kkStar = kkStar;
  }

  public String getKkStar() {
    return kkStar;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getGenre() {
    return genre;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getYear() {
    return year;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public boolean isStarred() {
    return starred;
  }

  public void setStarred(boolean starred) {
    this.starred = starred;
  }

  public boolean isUnread() {
    return unread;
  }

  public void setUnread(boolean unread) {
    this.unread = unread;
  }
}
