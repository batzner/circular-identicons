# Circular Identicons
Github's or Stackoverflow's identicons don't look good in circular profile pictures. Circular-identicons will generate gorgeous circular default profile pictures for your Android app.

## Examples
The identicons are circular kaleidoscopes.

![](http://i.imgur.com/GtGqFtr.png)

## Usage 

Place [IdenticonFactory.java](https://github.com/therealkilian/circular-identicons/blob/master/app/src/main/java/com/kilianbatzner/identicons/IdenticonFactory.java) in your project. The method to create an identicon is `createIdenticon(Bitmap original, int foregroundColor, int backgroundColor)`. 

```Java
Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.kaleidoscope_source);
Bitmap identicon = IdenticonFactory.createIdenticon(original, Color.BLACK, Color.WHITE);
// The identicon will have the same size as the original
```

You will need a bitmap that is used as a source to create the kaleidoscopic identicon. You can use the bitmap `kaleidoscope_source.png` from the sample project:

![](https://raw.githubusercontent.com/therealkilian/circular-identicons/master/app/src/main/res/drawable/kaleidoscope_source.png)

If you want to use your own source image, make sure to make it circular with a transparent background.

## Hash objects into identicons

If you would like to turn any object into an identicon, you can set the seed of `random` in `createIdenticon(...)` with the object's hash value.
