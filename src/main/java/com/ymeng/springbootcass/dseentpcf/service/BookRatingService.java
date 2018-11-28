package com.ymeng.springbootcass.dseentpcf.service;

import com.ymeng.springbootcass.dseentpcf.model.BookRating;

import java.util.List;
import java.util.UUID;

public interface BookRatingService {

    public void saveRating(BookRating bookRating);

    public BookRating getRating(UUID book_id, String author_code, int rating_year);

    public List<BookRating> getAllRatingsForBook(UUID book_id);
}
