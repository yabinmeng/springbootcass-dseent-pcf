package com.ymeng.springbootcass.dseentpcf.controller;


import com.ymeng.springbootcass.dseentpcf.model.BookRating;
import com.ymeng.springbootcass.dseentpcf.service.BookRatingService;
import com.ymeng.springbootcass.dseentpcf.util.CustomErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BookRatingController {

    @Autowired
    BookRatingService bookRatingService;

    public BookRatingController() {
        System.out.println("BookRatingController()");
    }

    @RequestMapping(value = "/rating/{bookId}_{authorCode}_{ratingYear}", method = RequestMethod.GET)
    public ResponseEntity<?> getBook(@PathVariable("bookId") String bookIdStr,
                                     @PathVariable("authorCode") String authorCode,
                                     @PathVariable("ratingYear") int ratingYear) {
        BookRating rating = bookRatingService.getRating(
            UUID.fromString(bookIdStr),
            authorCode,
            ratingYear
        );

        if (rating == null) {
            return new ResponseEntity(
                new CustomErrorType(
                    "BookRating with book_id (" + bookIdStr + "), " +
                        "author_code (" + authorCode + ")," +
                        "rating_year (" + ratingYear + ") not found"),
                HttpStatus.NOT_FOUND
            );
        }
        return new ResponseEntity<BookRating>(rating, HttpStatus.OK);
    }

    @RequestMapping(value = "/ratings/{bookId}", method = RequestMethod.GET)
    public ResponseEntity<List<BookRating>> listRatingsForBook(
        @PathVariable("bookId") String bookIdStr) {

        List<BookRating> ratings =
            bookRatingService.getAllRatingsForBook(UUID.fromString(bookIdStr));

        if (ratings.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
            // You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<List<BookRating>>(ratings, HttpStatus.OK);
    }


    @RequestMapping(value = "/bookrating", method = RequestMethod.POST)
    public ResponseEntity<?> createBook(@RequestBody BookRating bookRating, UriComponentsBuilder ucBuilder) {

        bookRatingService.saveRating(bookRating);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(
            ucBuilder
                .path("/api/bookrating/{bookId}_{authorCode}_{ratingYear}")
                .buildAndExpand(bookRating.getBookId(), bookRating.getAuthorCode(), bookRating.getRatingYear())
                .toUri());

        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }


}
