# mybooks-android
An application to keep track of the books I read. The serialized form (json) is saved to Dropbox.

# The why

It's been a while that I promised myself I would keep track of the books I read. 
Unable to prevent myself from losing papers and notes, I spent some time testing the online services, but I didn't find what I looked for, i.e.:

- *simple*: just store the book title, author and possibly some notes about it,
- *no validation*: if I want to add an obscure title which is not in the public databases, let me do it (example: the 
book my best friend wrote but never published)
- *private*: keep the list private,
- *exportable*: easily retrieve a list of the books in a common format like json, csv, etc.,
- *free*: I won't pay for it,
- *language agnostic*: as long as the charaters are part of the ASCII set,
- *nice*: it is not because it is free that it must be ugly.

Maybe it was too much to ask... Anyway, until I find the perfect one, I decided I would use my own.

# The what

**MyBooks** project is divided into two tools: 

1) a **command line interface** written in go to quickly edit my list,
2) an **Android application** to let me maintain my list on the go.


# data format 
In order for the data to be easily accessible, even without any tool, the data are saved to Dropbox in a `json file` properly indented.  It is possible to edit it with any text editor. The only important point is to respect the basic structure: 

```
{
  "1984": {
    "author": "Georges Orwell",
    "date": "long ago",
    "notes": "my favorite book",
    "title": "1984"
  },
  "1q84 livre 1": {
    "author": "Haruki Murakami",
    "date": "janvier 2016",
    "notes": "vachement déçue. ",
    "title": "1Q84 (livre 1)"
  }
 }
```

A book has a `title`, an `author`, and optional `date` and `notes`. The books are kept in a `map`, the keys being the normalized title of the book. The *normalization* of the title follows those simple steps:

1. title to lowercase;
2. replace every accented character by its unaccented counterpart (éè -> e, etc)
3. replace non letter/number character by spaces
4. replace multiple spaces by one;
5. trim.

# Application

The Android application uses the *Dropbox core API V2* to read/write the file. On start, the file is read and loaded in memory, then each change is directly written to Dropbox. It is compatible with tablets/wide screen devices.

# TODO

-  handle dropbox errors properly
- what should be done if no internet connection is available ?
- feature: automatic detection of extern file changes ? (vs sync button)
