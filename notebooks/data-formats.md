# Overview of available data formats

```clj
^{:nextjournal.clerk/visibility {:code :hide}}(ns data-format
  {:nextjournal.clerk/toc true}
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as cio]
            [tech.v3.dataset :as ds]
            [tech.v3.io :as io]
            [tech.v3.libs.parquet :as parquet]))
```

```clj

^{::clerk/visibility {:code :hide :result :hide}} (defn copy-uri-to-file [uri file]
  (with-open [in (cio/input-stream uri)
              out (cio/output-stream file)]
    (clojure.java.io/copy in out)))

^{::clerk/visibility {:code :hide :result :hide}} (defn head [n filename]
  (with-open [rdr (cio/reader filename)]
    (doall (take n (line-seq rdr)))))
```

Data engineers are no strangers to dealing with large sets of data. File formats make a difference in the performance and efficiency of data operations. In this blog post, we will discuss some of the most popular file formats: 
- Comma-separated values (CSV)
- JavaScript Object Notation (JSON)
- Extensible Markup Language (XML)
- Extensible Data Notation (EDN)
- Apache Avro
- Protocol Buffers
- Apache Parquet
- Apache Optimized Row Columnar (ORC)

We will use the Clojure programming language to illustrate each data format.

```clj
^{::clerk/visibility {:code :hide :result :hide}} 
(defn generate-random-data
  [number-of-columns number-of-rows]
  (let [xs (for [i (range number-of-columns)]
             (str "x" i))]
    (for [_ (range number-of-rows)]
      (reduce (fn [eax x] (assoc eax x (rand)))
              {}
              xs))))
```

## Comma-separated values (CSV)

A CSV file is a plain text file stat uses commas to separate values. 

## JavaScript Object Notation (JSON)

## Extensible Markup Language (XML)

## Extensible Data Notation (EDN)

## Apache Avro

## Protocol Buffers

## Apache Parquet

Apache Parquet is a columnar storage file format designed for efficiency and performance. It is well-suited for running complex queries on large datasets. Parquet is a common choice for structured data in data lakes because it is cost-effective and allows for selective column retrieval and aggregation.

### Apache Parquet is columnar-oriented
Parquet stores data by columns. It allows for a more efficient disk I/O and compression. It reduces the amount of data transferred from disk to memory, leading to faster query performance.

A Parquet file contains row groups. Each row group consists of column chunks for each column. Each column chuck holds multiple pages.

![parquet file format](https://github.com/pierregoudjo/data-engineering-primer/assets/1331326/e273b0f5-4128-4dc6-ba53-9220de120fc1)

Each Parquet file has one header, one or many data blocks, and one footer which contains the Parquet metadata.

![parquet metadata](https://github.com/pierregoudjo/data-engineering-primer/assets/1331326/a66f1869-f130-4931-af1b-7a3b7d41c77a)



### Apache Parquet leverages advanced compression techniques

Parquet allows the compression of the data blocks inside dictionary pages for better space efficiency. The Parquet format supports multiple compression codecs like Snappy, Gzip, LZO, or Brotli. The compression reduces the disk storage space and improves performance for columnar data retrieval.

![parquet compression stats](https://github.com/pierregoudjo/data-engineering-primer/assets/1331326/2bad5cb0-f72d-49e3-bad8-3f1ccbe123cb)


### Apache Parquet is widely supported in data ecosystems

Parquet is compatible with various big data processions frameworks and tools like:

- Apache Spark
- Apache Hive
- Amazon Athena
- Amazon Redshift
- Presto
- ...

This compatibility makes it a natural choice for data lakes, data warehouses, and analytics workloads.

### Apache Parquet supports limited schema evolution

Apache Parquet allows for column addition without rewriting the entire dataset. It also provides the ability to merge schemas that do not conflict. However, schema merging is expensive because it requires reading all the parquet files from top to bottom.

![schema evolution](https://github.com/pierregoudjo/data-engineering-primer/assets/1331326/215e9863-2e47-4d32-b999-09a5c94beb41)

### Apache Parquet is not well-suited for write-heavy workloads

Parquet performs column-wise compression and encoding. It makes writing data very costly and thus rules out Parquet for write-heavy workloads.

### Apache Parquet is not well-suited for small data sets

The columnar storage model starts showing its advantages with large data sets. Dealing with metadata, predicate pushdown, and other Parquet optimizations is pure overhead at a smaller scale. Gigabyte Parquet files are not uncommon.

### Example of data exploration with Parquet

Let's download the Titanic passenger survival dataset to a file named titanic.parquet:
```clj
(copy-uri-to-file "https://huggingface.co/api/datasets/phihung/titanic/parquet/default/train/0.parquet" "titanic.parquet")
```

The file only contains undecipherable binary data starting with the Parquet Header [magic number](https://en.wikipedia.org/wiki/Magic_number_(programming)#In_files) `PAR1`.

```clj
^{::clerk/auto-expand-results? true} (head 10 "titanic.parquet")
```

It is also possible to display the content of a Parquet file using a tool like [Visidata](https://www.visidata.org)

```shell
visidata data.parquet
```

![Visidata output](https://github.com/pierregoudjo/data-engineering-primer/assets/1331326/82891d2d-c9d2-4877-9ac9-b07715339adf)

Let's parse the `titanic.parquet` file and display the data by using the [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) library and its parquet support library

```clj
^{::clerk/viewer clerk/table}
(parquet/parquet->ds "titanic.parquet")
```

With the data at hand, we can select some of the columns. It is called data projection.

```clj
^{::clerk/viewer clerk/table}
(-> (parquet/parquet->ds "titanic.parquet")
    (ds/select-columns ["Name" "Sex"]))
```

Data projection is fast with Parquet because of its columnar nature:

```clj
(-> (parquet/parquet->ds "titanic.parquet")
    (ds/select-columns ["Name" "Sex"])
    time
    with-out-str)
```

Predicate selection is also fast on Parquet file thanks to the use of the [predicate pushdown](https://medium.com/@diehardankush/predicate-pushdown-in-parquet-enhancing-efficiency-and-performance-5becb0b992de) optimization technique:

```clj
^{::clerk/viewer clerk/table}
(-> (parquet/parquet->ds "titanic.parquet")
    (ds/filter-column "Sex" "female"))
```

By combining, predicate selection and data selection we can narrow the dataset to only the name of the survivors of the Titanic tragedy:

```clj
^{::clerk/viewer clerk/table}
(-> (parquet/parquet->ds "titanic.parquet")
    (ds/filter-column "Survived" 1)
    (ds/select-columns ["Name"]))
```

The columnar nature of Parquet and its optimization techniques allows for efficient disk I/O to get the desired data:

![parquet columnar](https://github.com/pierregoudjo/data-engineering-primer/assets/1331326/95d40c74-0587-466a-8cd8-87f3fa1b2ed1)

## Apache Optimized Row Columnar (ORC)
