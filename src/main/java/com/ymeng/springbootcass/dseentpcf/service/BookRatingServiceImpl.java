package com.ymeng.springbootcass.dseentpcf.service;

import com.ymeng.springbootcass.dseentpcf.model.BookRating;
import com.ymeng.springbootcass.dseentpcf.repository.BookRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookRatingServiceImpl implements BookRatingService {

    @Autowired
    BookRatingRepository bookRatingRepository;

    public BookRatingServiceImpl() {
        super();
    }

    @Override
    public void saveRating(BookRating bookRating) {
        bookRatingRepository.saveRating(bookRating);
    }

    @Override
    public BookRating getRating(UUID book_id, String author_code, int rating_year) {
        return bookRatingRepository.findRating(book_id, author_code, rating_year);
    }

    @Override
    public List<BookRating> getAllRatingsForBook(UUID book_id) {
        return bookRatingRepository.findRatingForBook(book_id);
    }
}
