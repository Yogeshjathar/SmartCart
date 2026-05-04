package com.smartcart.common.seed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class CatalogSeedData {

    private static final String[][] CATEGORY_PRODUCTS = {
            {"Wireless Earbuds", "Bluetooth Speaker", "Smart Watch", "Mechanical Keyboard", "Gaming Mouse",
                    "USB-C Hub", "Portable SSD", "Webcam", "Noise Cancelling Headphones", "Tablet Stand"},
            {"Sofa Throw", "Bedside Lamp", "Wall Clock", "Storage Basket", "Ceramic Vase",
                    "Floor Rug", "Curtain Set", "Desk Organizer", "Laundry Hamper", "Shelf Bracket"},
            {"Chef Knife", "Air Fryer", "Blender", "Coffee Grinder", "Cutting Board",
                    "Sauce Pan", "Spice Rack", "Water Bottle", "Lunch Box", "Food Container Set"},
            {"Yoga Mat", "Resistance Bands", "Dumbbell Set", "Foam Roller", "Skipping Rope",
                    "Kettlebell", "Workout Bench", "Running Belt", "Balance Ball", "Fitness Tracker Strap"},
            {"Notebook Set", "Gel Pen Pack", "Ergonomic Chair", "Monitor Riser", "Desk Mat",
                    "Whiteboard Kit", "Label Maker", "Document Tray", "Stapler", "Laptop Sleeve"},
            {"Camping Lantern", "Hiking Backpack", "Insulated Tumbler", "Portable Grill", "Foldable Chair",
                    "Picnic Blanket", "Trail Compass", "Waterproof Tarp", "Thermal Flask", "Travel Cooler"},
            {"Cotton T-Shirt", "Denim Jacket", "Running Shoes", "Leather Wallet", "Classic Cap",
                    "Linen Shirt", "Sports Socks", "Travel Hoodie", "Canvas Belt", "Weekend Tote"},
            {"Face Cleanser", "Body Lotion", "Hair Dryer", "Lip Balm", "Perfume Mist",
                    "Shampoo Set", "Beard Trimmer", "Sunscreen", "Makeup Brush Set", "Bath Salt Pack"},
            {"Building Blocks", "Remote Car", "Puzzle Board", "Action Figure", "Story Book",
                    "Science Kit", "Plush Bear", "Color Pencil Set", "Board Game", "Toy Train"},
            {"Organic Coffee", "Green Tea", "Granola Mix", "Peanut Butter", "Olive Oil",
                    "Trail Mix", "Pasta Pack", "Breakfast Oats", "Dark Chocolate", "Honey Jar"}
    };

    private static final String[] CATEGORIES = {
            "Electronics", "Home Decor", "Kitchen", "Fitness", "Office",
            "Outdoor", "Fashion", "Beauty", "Toys", "Grocery"
    };

    private static final String[] BRANDS = {
            "Nova", "EverPeak", "Luma", "Terra", "UrbanNest",
            "Pulse", "CraftedCo", "Summit", "Velora", "PureLeaf",
            "Aster", "BrightForge"
    };

    private static final String[] WAREHOUSES = {
            "Bengaluru-A1", "Mumbai-B2", "Delhi-C3", "Hyderabad-D4", "Chennai-E5"
    };

    private CatalogSeedData() {
    }

    public static List<CatalogSeedItem> items() {
        List<CatalogSeedItem> items = new ArrayList<>();

        for (int categoryIndex = 0; categoryIndex < CATEGORIES.length; categoryIndex++) {
            String category = CATEGORIES[categoryIndex];
            String[] productNames = CATEGORY_PRODUCTS[categoryIndex];

            for (int productIndex = 0; productIndex < productNames.length; productIndex++) {
                String baseName = productNames[productIndex];
                String brand = BRANDS[(categoryIndex + productIndex) % BRANDS.length];
                String productId = "seed-" + slug(category) + "-" + String.format("%02d", productIndex + 1);
                BigDecimal price = BigDecimal.valueOf(19 + (categoryIndex * 9L) + (productIndex * 4L) + 0.99);
                int quantity = 20 + ((categoryIndex * 7 + productIndex * 5) % 90);
                String warehouse = WAREHOUSES[(categoryIndex + productIndex) % WAREHOUSES.length];

                items.add(new CatalogSeedItem(
                        productId,
                        brand + " " + baseName,
                        baseName + " for everyday " + category.toLowerCase() + " needs.",
                        price,
                        "USD",
                        category,
                        brand,
                        quantity,
                        warehouse
                ));
            }
        }

        return List.copyOf(items);
    }

    private static String slug(String value) {
        return value.toLowerCase().replace(" ", "-");
    }
}
