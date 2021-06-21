This is a work-in-progress Java 5.5.1-to-7.0 converter.
Some parts are ported directly from the C converter (such as the ANSEL Charset and date and age parsing) while others are built from the ground up. The hope is that having two somewhat-separate implementations will allow me to use the two to test one another, a hope that has already resulted in a few bug fixes in the C version.

Current status:

- [x] Detect character encodings, as documented in [ELF Serialisation](https://fhiso.org/TR/elf-serialisation).
- [x] Convert to UTF-8
- [x] Normalize line whitespace, including stripping leading spaces
- [x] Remove `CONC`
- [x] Fix `@` usage
- [x] Limit character set of cross-reference identifiers
- [x] Normalize case of tags
- [x] Covert `DATE`
    - [x] replace date_phrase with `PHRASE` structure
    - [x] replace calendar escapes with calendar tags
    - [x] change `BC` and `B.C.` to `BCE` and remove if found in unsupported calendars
    - [x] replace dual years with single years and `PHRASE`s
    - [x] replace just-year dual years in unqualified date with `BET`/`AND`
- [x] Convert `AGE`
    - [x] change age words to canonical forms (stillborn as `0y`, child as `< 8y`, infant as `< 1y`) with `PHRASE`s
    - [x] Normalize spacing in `AGE` payloads
    - [x] add missing `y`
- [x] Change any illegal tag `XYZ` into `_EXT_XYZ`
- [x] change `SOUR` with text payload into pointer to `SOUR` with `NOTE`
- [x] change `OBJE` with no payload to pointer to new `OBJE` record
- [ ] change `NOTE` record or with pointer payload into `SNOTE`
    - [ ] use heuristic to change some pointer-`NOTE` to nested-`NOTE` instead of `SNOTE`
- [ ] Convert `LANG` payloads to BCP 47 tags, using [FHISO's mapping](https://github.com/fhiso/legacy-format/blob/master/languages.tsv)
- [ ] Convert `MEDI`.`FORM` payloads to media types
- [ ] Enumerated values
    - [ ] Normalize case
    - [ ] Convert user-text to `PHRASE`s
- [ ] Convert `FONE` and `ROMN` to `TRAN` and their `TYPE`s to BCP-47 `LANG`s
- [ ] tag renaming, including
    - `EMAI`, `_EMAIL` → `EMAIL`
    - `FORM`.`TYPE` → `FORM`.`MEDI`
    - (deferred) `_SDATE` → `SDATE` -- `_SDATE` is also used as "accessed at" date for web resources by some applications so this change is not universally correct
    - `_UID` → `UID`
    - `_ASSO` → `ASSO`
    - `_CRE`, `_CREAT` → `CREA`
    - `_DATE` → `DATE`
    - other?
- [ ] `ASSO`.`RELA` → `ASSO`.`ROLE` (changing payload OTHER + PHRASE)
- [ ] change `RFN`, `RIN`, and `AFN` to `EXID`
- [ ] change `_FSFTID`, `_APID` to `EXID`
- [ ] remove `SUBN`, `HEAD`.`FILE`, `HEAD`.`CHAR`
    - (deferred) `HEAD`.`PLAC` was originally on this list, but has been deferred to a later version
- [ ] change `FILE` payloads into URLs
    - [ ] Windows-style `\` becomes `/`
    - [ ] Windows diver letter `C:\WINDOWS` becomes `file:///c:/WINDOWS`
    - [ ] POSIX-stye `/User/foo` becomes `file:///User/foo`
- [x] update the `GEDC`.`VERS` to `7.0`
- [ ] (extra) change string-valued `INDI`.`ALIA` into `NAME` with `TYPE` `AKA`
- [ ] (5.5) change base64-encoded OBJE into GEDZIP
- [ ] add `SCHMA` for all used known extensions
    - [ ] add URIs (or standard tags) for all extensions from <https://wiki-de.genealogy.net/GEDCOM/_Nutzerdef-Tag> and <http://www.gencom.org.nz/GEDCOM_tags.html>

