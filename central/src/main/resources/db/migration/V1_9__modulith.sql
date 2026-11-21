-- Event publication registry của Spring Modulith 1.4 (spring-modulith-events-jpa).
-- Tạo bằng Flyway vì spring.jpa.hibernate.ddl-auto=validate. Lưu các sự kiện QrCredentialChanged,
-- StationConfigChanged, SigningKeyPublished để giao lại sau khi khởi động lại.

CREATE TABLE event_publication (
  id               UUID                     NOT NULL PRIMARY KEY,
  listener_id      TEXT                     NOT NULL,
  event_type       TEXT                     NOT NULL,
  serialized_event TEXT                     NOT NULL,
  publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date  TIMESTAMP WITH TIME ZONE
);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);

-- Dùng khi spring.modulith.events.completion-mode=archive; tạo sẵn để Hibernate validate không lỗi.
CREATE TABLE event_publication_archive (
  id               UUID                     NOT NULL PRIMARY KEY,
  listener_id      TEXT                     NOT NULL,
  event_type       TEXT                     NOT NULL,
  serialized_event TEXT                     NOT NULL,
  publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date  TIMESTAMP WITH TIME ZONE
);
CREATE INDEX event_publication_archive_by_completion_date_idx ON event_publication_archive (completion_date);
